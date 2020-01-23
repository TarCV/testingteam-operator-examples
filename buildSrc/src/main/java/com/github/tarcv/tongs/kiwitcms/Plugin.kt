package com.github.tarcv.tongs.kiwitcms

import com.github.tarcv.tongs.model.Pool
import com.github.tarcv.tongs.runner.rules.*
import com.github.tarcv.tongs.summary.ResultStatus
import java.util.*

public class KiwiReporterRunRuleFactory
    : TestCaseRunRuleFactory<KiwiReporterRunRule>, PoolRunRuleFactory<KiwiReporterPoolRule>,
        HasConfiguration
{
    override val configurationSections: Array<String> = arrayOf("kiwi")

    private val emitters = Collections.synchronizedMap(HashMap<Pool, NewTestDataEmitter>())

    override fun testCaseRunRules(context: TestCaseRunRuleContext): Array<out KiwiReporterRunRule> {
        return arrayOf(KiwiReporterRunRule {
            val pool: Pool = context.pool
            println("KiwiReporterRunRule producer - $pool")
            val newTestDataEmitter: NewTestDataEmitter? = emitters[pool]
            newTestDataEmitter!!
        })
    }

    override fun poolRules(context: PoolRunRuleContext): Array<out KiwiReporterPoolRule> {
        return arrayOf(KiwiReporterPoolRule(context.configuration.pluginConfiguration, context.pool, emitters))
    }
}

public class KiwiReporterRunRule(
    private val emitterProvider: () -> NewTestDataEmitter
) : TestCaseRunRule {

    override fun after(arguments: TestCaseRunRuleAfterArguments) {
        val emitter = emitterProvider()

        // TODO: only add test case to a run at the last attempt
        // TODO: correctly handle multiple JUnit cases representing one case in TCMS
        emitter.addResultsToRun(listOf(arguments.result))
    }

    private fun convertStatus(status: ResultStatus): String {
        return if (status == ResultStatus.PASS) {
            "PASS"
        } else {
            "FAIL"
        }
    }

    override fun before() {
        // no op
    }
}

class KiwiReporterPoolRule(
    private val configuration: Map<String, Any>,
    private val pool: Pool,
    private val emitters: MutableMap<Pool, NewTestDataEmitter>
) : PoolRunRule {
    override fun after() {
        println("com.github.tarcv.tongs.kiwitcms.KiwiReporterPoolRule.after - $pool")
        val emitter = emitters.remove(pool)!!
        emitter.closeSession()
    }

    override fun before() {
        println("com.github.tarcv.tongs.kiwitcms.KiwiReporterPoolRule.before - $pool")
        emitters[pool] = NewTestDataEmitter(
            configuration["login"].toString(),
            configuration["password"].toString(),
            configuration["productName"].toString(),
            configuration["versionName"].toString(),
            configuration["planName"].toString(),
            configuration["buildName"].toString()
        )
    }

}
