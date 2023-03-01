package com.github.tarcv.tongs.kiwitcms

import com.github.tarcv.tongs.Configuration
import com.github.tarcv.tongs.injector.ActualConfiguration
import com.github.tarcv.tongs.api.devices.Device
import com.github.tarcv.tongs.api.devices.Pool
import com.github.tarcv.tongs.api.result.TestCaseFileManager
import com.github.tarcv.tongs.api.result.TestCaseRunResult
import com.github.tarcv.tongs.api.run.PoolRunRuleContext
import com.github.tarcv.tongs.api.run.ResultStatus
import com.github.tarcv.tongs.api.run.RunConfiguration
import com.github.tarcv.tongs.api.run.TestCaseEvent
import com.github.tarcv.tongs.api.run.TestCaseRunRuleAfterArguments
import com.github.tarcv.tongs.api.run.TestCaseRunRuleContext
import com.github.tarcv.tongs.api.testcases.AnnotationInfo
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.MethodRule
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit


class KiwiReporterRunRuleTest {
    @JvmField
    val configurationMock: RunConfiguration? = ActualConfiguration(Configuration.aConfigurationBuilder()
        .withPluginConfiguration(mapOf(
                "kiwi" to mapOf(
                "baseUrl" to System.getenv("KIWI_HOST"),
                "login" to System.getenv("KIWI_LOGIN"),
                "password" to System.getenv("KIWI_PASS"),
                "productName" to System.getenv("KIWI_PRODUCT"),
                "planName" to System.getenv("KIWI_PLAN"),
                "versionName" to System.getenv("GIT_SHA"),
                "buildName" to System.getenv("BUILD_NUM"),
                    "any" to "18"
            )
        ))
        .build())

    @field:Mock
    @JvmField
    val fileMock: TestCaseFileManager? = null

    @field:Mock
    @JvmField
    val deviceMock: Device? = null

    @get:Rule
    var mockitoRule: MethodRule = MockitoJUnit.rule()

    // TODO: rewrite to not use actual JSONRPC client
    //    @Test // uncomment to test interaction with KiwiTCMS instance
    fun test() {
        val pool = Pool.Builder()
            .withName("testPool")
            .build()
        val annotations = listOf(
            AnnotationInfo("something.Link", mapOf("value" to "https://127.0.0.1:8443/case/1"))
        )
        val testCaseEvent = TestCaseEvent.newTestCase("method", "Class", emptyMap(),
            annotations, emptyList())

        val factory = KiwiReporterRunRuleFactory()
        val poolRule = factory.poolRules(
            PoolRunRuleContext(
                configurationMock!!,
                pool
            )
        )[0]
        val testCaseRunRule = factory.testCaseRunRules(
            TestCaseRunRuleContext(
                configurationMock,
                fileMock!!,
                pool,
                deviceMock!!,
                testCaseEvent
            )
        )[0]

        poolRule.before()
        testCaseRunRule.before()
        testCaseRunRule.after(TestCaseRunRuleAfterArguments(TestCaseRunResult(
            pool,
            deviceMock,
            testCaseEvent.testCase,
            ResultStatus.PASS,
            "",
            1f,
            0,
            emptyMap(),
            null,
            emptyList()
        )))
        poolRule.after()
    }
}

@RunWith(Parameterized::class)
class KiwiReporterRunRuleFoldTest(
    private val a: ResultStatus,
    private val b: ResultStatus,
    private val expectedResult: ResultStatus
) {
    @Test
    fun foldStatusesAB() {
        Assert.assertEquals(expectedResult, KiwiReporterRunRule.foldStatuses(a, b))
    }

    @Test
    fun foldStatusesBA() {
        Assert.assertEquals(expectedResult, KiwiReporterRunRule.foldStatuses(a, b))
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}, {1} -- {2}")
        fun data() = arrayOf(
            arrayOf(ResultStatus.PASS, ResultStatus.PASS, ResultStatus.PASS),
            arrayOf(ResultStatus.PASS, ResultStatus.FAIL, ResultStatus.FAIL),
            arrayOf(ResultStatus.PASS, ResultStatus.ERROR, ResultStatus.ERROR),
            arrayOf(ResultStatus.PASS, ResultStatus.IGNORED, ResultStatus.PASS),
            arrayOf(ResultStatus.PASS, ResultStatus.ASSUMPTION_FAILED, ResultStatus.UNKNOWN),
            arrayOf(ResultStatus.PASS, ResultStatus.UNKNOWN, ResultStatus.UNKNOWN),
            arrayOf(ResultStatus.FAIL, ResultStatus.PASS, ResultStatus.FAIL),
            arrayOf(ResultStatus.FAIL, ResultStatus.FAIL, ResultStatus.FAIL),
            arrayOf(ResultStatus.FAIL, ResultStatus.ERROR, ResultStatus.ERROR),
            arrayOf(ResultStatus.FAIL, ResultStatus.IGNORED, ResultStatus.FAIL),
            arrayOf(ResultStatus.FAIL, ResultStatus.ASSUMPTION_FAILED, ResultStatus.FAIL),
            arrayOf(ResultStatus.FAIL, ResultStatus.UNKNOWN, ResultStatus.FAIL),
            arrayOf(ResultStatus.ERROR, ResultStatus.PASS, ResultStatus.ERROR),
            arrayOf(ResultStatus.ERROR, ResultStatus.FAIL, ResultStatus.ERROR),
            arrayOf(ResultStatus.ERROR, ResultStatus.ERROR, ResultStatus.ERROR),
            arrayOf(ResultStatus.ERROR, ResultStatus.IGNORED, ResultStatus.ERROR),
            arrayOf(ResultStatus.ERROR, ResultStatus.ASSUMPTION_FAILED, ResultStatus.ERROR),
            arrayOf(ResultStatus.ERROR, ResultStatus.UNKNOWN, ResultStatus.ERROR),
            arrayOf(ResultStatus.IGNORED, ResultStatus.PASS, ResultStatus.PASS),
            arrayOf(ResultStatus.IGNORED, ResultStatus.FAIL, ResultStatus.FAIL),
            arrayOf(ResultStatus.IGNORED, ResultStatus.ERROR, ResultStatus.ERROR),
            arrayOf(ResultStatus.IGNORED, ResultStatus.IGNORED, ResultStatus.IGNORED),
            arrayOf(ResultStatus.IGNORED, ResultStatus.ASSUMPTION_FAILED, ResultStatus.UNKNOWN),
            arrayOf(ResultStatus.IGNORED, ResultStatus.UNKNOWN, ResultStatus.UNKNOWN),
            arrayOf(ResultStatus.ASSUMPTION_FAILED, ResultStatus.PASS, ResultStatus.UNKNOWN),
            arrayOf(ResultStatus.ASSUMPTION_FAILED, ResultStatus.FAIL, ResultStatus.FAIL),
            arrayOf(ResultStatus.ASSUMPTION_FAILED, ResultStatus.ERROR, ResultStatus.ERROR),
            arrayOf(ResultStatus.ASSUMPTION_FAILED, ResultStatus.IGNORED, ResultStatus.UNKNOWN),
            arrayOf(ResultStatus.ASSUMPTION_FAILED, ResultStatus.ASSUMPTION_FAILED, ResultStatus.UNKNOWN),
            arrayOf(ResultStatus.ASSUMPTION_FAILED, ResultStatus.UNKNOWN, ResultStatus.UNKNOWN),
            arrayOf(ResultStatus.UNKNOWN, ResultStatus.PASS, ResultStatus.UNKNOWN),
            arrayOf(ResultStatus.UNKNOWN, ResultStatus.FAIL, ResultStatus.FAIL),
            arrayOf(ResultStatus.UNKNOWN, ResultStatus.ERROR, ResultStatus.ERROR),
            arrayOf(ResultStatus.UNKNOWN, ResultStatus.IGNORED, ResultStatus.UNKNOWN),
            arrayOf(ResultStatus.UNKNOWN, ResultStatus.ASSUMPTION_FAILED, ResultStatus.UNKNOWN),
            arrayOf(ResultStatus.UNKNOWN, ResultStatus.UNKNOWN, ResultStatus.UNKNOWN)
        )
    }
}