package com.github.tarcv.tongs.kiwitcms

import com.github.tarcv.tongs.TongsConfiguration
import com.github.tarcv.tongs.model.Device
import com.github.tarcv.tongs.model.Pool
import com.github.tarcv.tongs.model.TestCaseEvent
import com.github.tarcv.tongs.runner.TestCaseRunResult
import com.github.tarcv.tongs.runner.rules.PoolRunRuleContext
import com.github.tarcv.tongs.runner.rules.TestCaseRunRuleAfterArguments
import com.github.tarcv.tongs.runner.rules.TestCaseRunRuleContext
import com.github.tarcv.tongs.summary.ResultStatus
import com.github.tarcv.tongs.system.io.TestCaseFileManager
import org.junit.Rule
import org.junit.Test
import org.junit.rules.MethodRule
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import javax.net.ssl.HttpsURLConnection


class KiwiReporterRunRuleTest {
    @field:Mock
    @JvmField
    val configurationMock: TongsConfiguration? = null

    @field:Mock
    @JvmField
    val fileMock: TestCaseFileManager? = null

    @field:Mock
    @JvmField
    val deviceMock: Device? = null

    @get:Rule
    var mockitoRule: MethodRule = MockitoJUnit.rule()

    fun test() {
        val pool = Pool.Builder()
            .withName("testPool")
            .build()
        val testCaseEvent = TestCaseEvent.newTestCase("method", "Class", emptyMap(), emptyList(), emptyList())

        val factory = KiwiReporterRunRuleFactory()
        val poolRule = factory.poolRules(
            PoolRunRuleContext(
                configurationMock!!,
                fileMock!!,
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
        testCaseRunRule.after(TestCaseRunRuleAfterArguments(TestCaseRunResult(pool, deviceMock, testCaseEvent.testCase, ResultStatus.PASS, "", 1f, 0, emptyMap(), null, emptyList())))
        poolRule.after()
    }
}