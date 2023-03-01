package com.github.tarcv.tongs.kiwitcms

import com.github.tarcv.tongs.api.HasConfiguration
import com.github.tarcv.tongs.api.devices.Pool
import com.github.tarcv.tongs.api.run.PoolRunRule
import com.github.tarcv.tongs.api.run.PoolRunRuleContext
import com.github.tarcv.tongs.api.run.PoolRunRuleFactory
import com.github.tarcv.tongs.api.run.ResultStatus
import com.github.tarcv.tongs.api.run.TestCaseEvent
import com.github.tarcv.tongs.api.run.TestCaseRunRule
import com.github.tarcv.tongs.api.run.TestCaseRunRuleAfterArguments
import com.github.tarcv.tongs.api.run.TestCaseRunRuleContext
import com.github.tarcv.tongs.api.run.TestCaseRunRuleFactory
import com.github.tarcv.tongs.api.testcases.TestCase
import com.github.tarcv.tongs.api.testcases.TestCaseRule
import com.github.tarcv.tongs.api.testcases.TestCaseRuleContext
import com.github.tarcv.tongs.api.testcases.TestCaseRuleFactory
import java.lang.RuntimeException
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.max

class KiwiReporterRunRuleFactory
    : TestCaseRunRuleFactory<KiwiReporterRunRule>, PoolRunRuleFactory<KiwiReporterPoolRule>,
        TestCaseRuleFactory<KiwiReporterTestCaseRule>,
        HasConfiguration
{
    private val pluginSectionName = "kiwi"

    override val configurationSections: Array<String> = arrayOf(pluginSectionName)

    private val caseInfos = Collections.synchronizedMap(HashMap<Int, CmsCaseMappingAccumulator>())

    private val emitters = Collections.synchronizedMap(HashMap<Pool, DataEmitter>())

    override fun testCaseRules(context: TestCaseRuleContext): Array<out KiwiReporterTestCaseRule> {
        return arrayOf(KiwiReporterTestCaseRule(caseInfos))
    }

    override fun testCaseRunRules(context: TestCaseRunRuleContext): Array<out KiwiReporterRunRule> {
        val pool: Pool = context.pool
        return arrayOf(KiwiReporterRunRule(pool, caseInfos) {
            println("KiwiReporterRunRule producer - $pool")
            val dataEmitter: DataEmitter? = emitters[pool]
            dataEmitter!!
        })
    }

    override fun poolRules(context: PoolRunRuleContext): Array<out KiwiReporterPoolRule> {
        val configuration =
            context.configuration.pluginConfiguration[pluginSectionName] as Map<String, String>
        return arrayOf(KiwiReporterPoolRule(configuration, context.pool, emitters))
    }
}

class KiwiReporterTestCaseRule(private val caseInfos: MutableMap<Int, CmsCaseMappingAccumulator>)
    : TestCaseRule {
    override fun transform(testCaseEvent: TestCaseEvent): TestCaseEvent {
        val testCase = testCaseEvent.testCase
        extractTestCaseId(testCase)
            ?.let { caseId ->
                caseInfos.computeIfAbsent(caseId) { CmsCaseMappingAccumulator() }
                    .addMappedCase(testCase)
            }

        return testCaseEvent
    }
}

class KiwiReporterRunRule(
    private val pool: Pool,

    // TODO: init rules just before using, to remove the need for this lambda
    private val caseInfos: Map<Int, CmsCaseMappingAccumulator>,
    private val emitterProvider: () -> DataEmitter
) : TestCaseRunRule {
    private lateinit var emitter: DataEmitter

    override fun before() {
        emitter = emitterProvider()
        val cases = caseInfos.keys
        emitter.addCasesToPlan(cases)
    }

    override fun after(arguments: TestCaseRunRuleAfterArguments) {
        // TODO: only add test case to a run at the last attempt
        synchronized(caseInfos) {
            val result = arguments.result
            val testCase = result.testCase
            val caseId = extractTestCaseId(testCase)
            if (caseId != null) {
                caseInfos[caseId]?.apply {
                    addResult(pool, testCase, result.status)
                    accumulate(pool) { results ->
                        val allResults = results.values
                        val overallStatus =
                            allResults.fold(allResults.first()) { variantResult, acc ->
                                foldStatuses(acc, variantResult)
                            }

                        emitter.addResultToRun(
                            caseId,
                            convertStatus(overallStatus),
                            "${testCase.testClass}#${testCase.testMethod} - ${result.status}"
                        )
                    }
                }
            }
        }
    }

    companion object {
        fun convertStatus(status: ResultStatus?): KiwiApi.TestExecutionStatus {
            return when(status) {
                null -> KiwiApi.TestExecutionStatus.IDLE
                ResultStatus.PASS -> KiwiApi.TestExecutionStatus.PASS
                else -> KiwiApi.TestExecutionStatus.FAIL
            }
        }

        fun foldStatuses(
            acc: ResultStatus?,
            result: ResultStatus?
        ): ResultStatus? {
            val statusIndexes = arrayOf(
                ResultStatus.IGNORED,
                ResultStatus.PASS,
                null,
                ResultStatus.FAIL,
                ResultStatus.ERROR
            )

            fun statusToInt(status: ResultStatus?): Int {
                return status
                    .let {
                        if (it == ResultStatus.ASSUMPTION_FAILED) {
                            null
                        } else {
                            it
                        }
                    }
                    .let { statusIndexes.indexOf(it) }
            }

            val accIndex = statusToInt(acc)
            val resultIndex = statusToInt(result)
            val totalIndex = max(accIndex, resultIndex)
            return statusIndexes[totalIndex]
        }
    }
}

class KiwiReporterPoolRule(
    private val configuration: Map<String, Any>,
    private val pool: Pool,
    private val emitters: MutableMap<Pool, DataEmitter>
) : PoolRunRule {
    override fun after() {
        println("com.github.tarcv.tongs.kiwitcms.KiwiReporterPoolRule.after - $pool")
        emitters.remove(pool)?.apply {
            completeRun()
            closeSession()
        }
    }

    override fun before() {
        println("com.github.tarcv.tongs.kiwitcms.KiwiReporterPoolRule.before - $pool")
        println("com.github.tarcv.tongsi.kiwitcms.KiwiReporterPoolRule.before - config: $configuration")
        emitters[pool] = DataEmitter(
            configuration["baseUrl"].toString(),
            configuration["login"].toString(),
            configuration["password"].toString(),
            configuration["productName"].toString(),
            configuration["versionName"].toString(),
            configuration["planName"].toString(),
            configuration["buildName"].toString()
        )
    }

}

internal fun extractTestCaseId(testCase: TestCase): Int? {
    val urlPattern = Regex(".+/case/(\\d+)/?$")
    val testCaseLink = testCase.annotations
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
