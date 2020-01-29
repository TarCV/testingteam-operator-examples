package com.github.tarcv.tongs.kiwitcms

import com.github.tarcv.tongs.Configuration
import com.github.tarcv.tongs.injector.ActualConfiguration
import com.github.tarcv.tongs.model.AnnotationInfo
import com.github.tarcv.tongs.model.Device
import com.github.tarcv.tongs.model.Pool
import com.github.tarcv.tongs.model.TestCaseEvent
import com.github.tarcv.tongs.runner.TestCaseRunResult
import com.github.tarcv.tongs.runner.rules.PoolRunRuleContext
import com.github.tarcv.tongs.runner.rules.RunConfiguration
import com.github.tarcv.tongs.runner.rules.TestCaseRunRuleAfterArguments
import com.github.tarcv.tongs.runner.rules.TestCaseRunRuleContext
import com.github.tarcv.tongs.summary.ResultStatus
import com.github.tarcv.tongs.system.io.TestCaseFileManager
import org.junit.Rule
import org.junit.rules.MethodRule
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit


class KiwiReporterRunRuleTest {
    @JvmField
    val configurationMock: RunConfiguration? = ActualConfiguration(Configuration.aConfigurationBuilder()
        .withPluginConfiguration(mapOf(
            "baseUrl" to System.getenv("KIWI_HOST"),
            "login" to System.getenv("KIWI_LOGIN"),
            "password" to System.getenv("KIWI_PASS"),
            "productName" to System.getenv("KIWI_PRODUCT"),
            "planName" to System.getenv("KIWI_PLAN"),
            "versionName" to System.getenv("GIT_SHA"),
            "buildName" to System.getenv("BUILD_NUM")
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

//    @Test
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