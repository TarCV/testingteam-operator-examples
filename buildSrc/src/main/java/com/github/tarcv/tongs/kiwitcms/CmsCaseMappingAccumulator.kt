package com.github.tarcv.tongs.kiwitcms

import com.github.tarcv.tongs.model.Pool
import com.github.tarcv.tongs.model.TestCase
import com.github.tarcv.tongs.summary.ResultStatus
import java.util.HashSet
import javax.annotation.concurrent.GuardedBy
import javax.annotation.concurrent.ThreadSafe

typealias CaseMappingKey = Pair<Pool, TestCase>

@ThreadSafe
class CmsCaseMappingAccumulator {
    @GuardedBy("this")
    private val knownCases = HashSet<TestCase>()

    @GuardedBy("this")
    private val knownPools = HashSet<Pool>()

    @GuardedBy("this")
    private val mappedResults = HashMap<CaseMappingKey, ResultStatus>()

    fun addMappedCase(testCase: TestCase) {
        synchronized(this) {
            knownCases.add(testCase)
        }
    }

    fun addResult(pool: Pool, testCase: TestCase, result: ResultStatus) {
        synchronized(this) {
            knownPools.add(pool)

            if (testCase in knownCases) {
                mappedResults[CaseMappingKey(pool, testCase)] = result
            }
        }
    }

    fun accumulate(pool: Pool, consumer: (Map<TestCase, ResultStatus>) -> Unit) {
        val accumulatedResults = synchronized(this) {
            val defaultResults = knownCases
                .map { testCase -> testCase to ResultStatus.UNKNOWN }
                .asSequence()
            val actualResults = mappedResults
                .filter { it.key.first == pool }
                .map { it.key.second to it.value }
                .asSequence()

            return@synchronized (defaultResults + actualResults)
                .associateBy({ it.first }, { it.second })
        }
        consumer.invoke(accumulatedResults)
    }
}
