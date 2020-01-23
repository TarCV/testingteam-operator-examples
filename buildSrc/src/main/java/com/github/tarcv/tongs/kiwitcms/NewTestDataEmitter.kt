package com.github.tarcv.tongs.kiwitcms

import com.github.tarcv.tongs.kiwitcms.KiwiApi.ProductBuildFilter
import com.github.tarcv.tongs.runner.TestCaseRunResult
import com.github.tarcv.tongs.summary.ResultStatus
import java.lang.RuntimeException
import javax.net.ssl.HttpsURLConnection

class NewTestDataEmitter(
    login: String,
    password: String,
    productName: String,
    versionName: String,
    planName: String,
    buildName: String
) {
    private val client: KiwiService by lazy { KiwiService(login, password) }

    private val buildId by lazy {
        val existingBuilds = client.getBuilds(ProductBuildFilter(productId, buildName))
        if (existingBuilds.isNotEmpty()) {
            existingBuilds[0].id
        } else {
            val newBuild = client.createBuild(buildName, productId)
            newBuild.id
        }
    }

    private val runId by lazy {
        client.createNewRun(
            buildId,
            login,
            planId,
            "Results for $productName, $versionName, $buildName"
        ).id
    }

    private val productId by lazy {
        client.getProductId(productName)
            ?: client.createNewProduct(productName).id
    }

    private val versionId by lazy {
        val existingVersions = client.getVersions(KiwiApi.ProductVersionFilter(
            productId,
            versionName
        ))
        if (existingVersions.isNotEmpty()) {
            existingVersions[0].id
        } else {
            val newVersion = client.createProductVersion(versionName, productId)
            newVersion.id
        }
    }

    private val planId by lazy {
        // check for name duplication
        client.getTestPlanId(planName, productId)
            .let {
                if (it < 0) {
                    client.createNewTP(productId, planName, versionId).id
                } else {
                    it
                }
            }
    }

    fun addResultsToRun(results: List<TestCaseRunResult>) {
        val testPlanId = planId

        for (result in results) {
            val testCaseId = extractTestCaseId(result)
            if (testCaseId != null) {
                client.addTestCaseToPlan(testPlanId, testCaseId)
                // TODO: this method should accept casesInTestRun and not call RPC if the TestCase
                // has already been added inside the TestRun
                val testExecution = client.addTestCaseToRunId(runId, testCaseId)
                client.updateTestExecution(testExecution.runId, convertStatus(result.status))
            }
        }
    }

    private fun convertStatus(status: ResultStatus): KiwiApi.TestExecutionStatus {
        return when(status) {
            ResultStatus.PASS -> KiwiApi.TestExecutionStatus.PASS
            else -> KiwiApi.TestExecutionStatus.FAIL
        }
    }

    private fun extractTestCaseId(test: TestCaseRunResult): Int? {
        val urlPattern = Regex(".+/case/(\\d+)/?$")
        val testCaseLink = test.testCase.annotations
            .singleOrNull { it.fullyQualifiedName.endsWith(".Link") }
            ?.properties?.get("value")
            ?.toString()

        return if (testCaseLink != null) {
            urlPattern.matchEntire(testCaseLink)
                ?.destructured
                ?.let { (caseId) ->
                    caseId.toInt()
                }
                ?: throw RuntimeException("Incorrect link: $testCaseLink")
        } else {
            null
        }
    }

    fun closeSession() {
        client.close()
    }

    companion object {
        init {
            HttpsURLConnection.setDefaultHostnameVerifier { hostname, _ -> hostname == "127.0.0.1" }
        }
    }
}