package com.github.tarcv.tongs.kiwitcms

import com.github.tarcv.tongs.kiwitcms.KiwiApi.ProductBuildFilter
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class DataEmitter(
    baseUrl: String,
    login: String,
    password: String,
    productName: String,
    versionName: String,
    planName: String,
    buildName: String
) {
    private val client: KiwiService by lazy { KiwiService(login, password, baseUrl) }

    private val buildId: Int by lazy {
        val existingBuilds = client.getBuilds(ProductBuildFilter(productId, buildName))
        if (existingBuilds.isNotEmpty()) {
            existingBuilds[0].id!!
        } else {
            val newBuild = client.createBuild(buildName, productId)
            newBuild.id!!
        }
    }

    private val runId: Int by lazy {
        client.createNewRun(
            buildId,
            login,
            planId,
            "Results for $productName, $versionName, $buildName"
        ).id!!
    }

    private val productId: Int by lazy {
        client.getProductId(productName)
            ?: client.createNewProduct(productName).id!!
    }

    private val versionId: Int by lazy {
        val existingVersions = client.getVersions(KiwiApi.ProductVersionFilter(
            productId,
            versionName
        ))
        if (existingVersions.isNotEmpty()) {
            existingVersions[0].id!!
        } else {
            val newVersion = client.createProductVersion(versionName, productId)
            newVersion.id!!
        }
    }

    private val planId: Int by lazy {
        // check for name duplication
        client.getTestPlanId(planName, productId)
            .let {
                if (it < 0) {
                    client.createNewTP(productId, planName, versionId).id!!
                } else {
                    it
                }
            }
    }

    fun addResultToRun(
        testCaseId: Int,
        result: KiwiApi.TestExecutionStatus,
        comment: String
    ) {
        safeAddCaseToPlan(testCaseId)

        val testExecution = client.getExecutions(runId, testCaseId)
            .firstOrNull()
            ?: client.addTestCaseToRunId(runId, testCaseId)
        client.updateTestExecution(testExecution.caseRunId!!, result, comment)
    }

    fun completeRun() {
        client.completeRun(runId)
    }

    fun closeSession() {
        client.close()
    }

    fun addCasesToPlan(testCases: Collection<Int>) {
        testCases.forEach(this@DataEmitter::safeAddCaseToPlan)
    }

    private fun safeAddCaseToPlan(it: Int) {
        try {
            client.addTestCaseToPlan(planId, it)
        } catch (e: Exception) {
            e.printStackTrace() // TODO
        }
    }

    companion object {
        init {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun getAcceptedIssuers(): Array<X509Certificate>? {
                    return null
                }

                override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) {}
                override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) {}
            })

            // Install the all-trusting trust manager
            try {
                val sc = SSLContext.getInstance("TLS")
                sc.init(null, trustAllCerts, SecureRandom())
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)
            } catch (e: Exception) {
            }


            HttpsURLConnection.setDefaultHostnameVerifier { hostname, _ -> hostname == "127.0.0.1" }
        }
    }
}