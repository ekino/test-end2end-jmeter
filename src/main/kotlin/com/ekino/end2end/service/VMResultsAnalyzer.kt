package com.ekino.end2end.service

import com.ekino.end2end.dto.ITestResult
import com.ekino.end2end.dto.VMTestResult
import java.util.*

class VMResultsAnalyzer : IRequestsAnalyzer {

    companion object {
        private val METRICS = listOf("CPUUtilization", "MemoryUtilization")
        private val SERVICES = listOf("service_client"
        // TODO add here all the VM services you want to check
        )
    }

    override fun parse(results: List<ITestResult>) : List<List<Any>> {
        val dataToPrint: MutableList<List<Any>> = mutableListOf()

        val referenceDate = Date().toString()

        SERVICES.forEach{ service ->
            METRICS.forEach { metric ->
                dataToPrint.add(
                        parseSingleComponent(
                                service,
                                metric,
                                referenceDate,
                                results.map {result -> result as VMTestResult }.filter { result -> metric == result.metricName && service == result.serviceName }))
            }
        }

        return dataToPrint
    }

    private fun parseSingleComponent(service: String, metric: String, date: String, results: List<VMTestResult>) : List<String> {
        return listOf(
                date,
                service,
                metric,
                calculateMean(results).toString(),
                calculateStandardDeviation(results).toString()
        )
    }

    private fun calculateMean(results: List<VMTestResult>) : Double {
        return results
                .map(VMTestResult::value)
                .average()
    }

    private fun calculateStandardDeviation(results: List<VMTestResult>) : Double {
        val average = calculateMean(results)

        val sum = results
                .map(VMTestResult::value)
                .map { value -> Math.pow(value - average, 2.0) }
                .sum()

        return Math.sqrt(sum / (results.size - 1))
    }
}