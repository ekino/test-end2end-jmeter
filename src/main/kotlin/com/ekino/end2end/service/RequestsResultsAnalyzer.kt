package com.ekino.end2end.service

import com.ekino.end2end.dto.ITestResult
import com.ekino.end2end.dto.RequestTestResult
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.util.*
import kotlin.streams.toList

class RequestsResultsAnalyzer : IRequestsAnalyzer {

    companion object {
        private val MC = MathContext(4, RoundingMode.HALF_UP)

        private val COMPONENTS: List<Pair<String, (RequestTestResult) -> Boolean>> = listOf(
                "homepage" to { result -> result.requestUrl.endsWith("www.homepage.com")}
        // TODO insert here the different regexes to identify the different workflows
                )
    }

    override fun parse(results: List<ITestResult>) : List<List<Any>> {

        val dataToPrint: MutableList<List<Any>> = mutableListOf()

        val referenceDate = Date().toString()

        COMPONENTS.forEach{
            (name, predicate) ->
                dataToPrint.add(parseSingleComponent(name, referenceDate, results.stream().map {result -> result as RequestTestResult }.filter(predicate).toList()))
        }

        return dataToPrint
    }

    private fun parseSingleComponent(component: String, date: String, results: List<RequestTestResult>) : List<Any> {
        return listOf(
                date,
                component,
                countRequests(results),
                errorPercentage(results),
                findPercentile(80, results),
                findPercentile(90, results),
                findPercentile(95, results),
                findPercentile(99, results)
        )
    }

    private fun countRequests(results: List<RequestTestResult>) : Int {
        return results.size
    }

    private fun errorPercentage(results: List<RequestTestResult>) : String {
        if (results.isEmpty()) {
            return "0 %"
        }

        val kos = results.stream().filter { result -> "false" == result.responseStatus }.count()
        val dividend = kos.toBigDecimal().multiply(BigDecimal.valueOf(100), MC)
        val division = dividend.divide(results.size.toBigDecimal(), MC)
        return division.toPlainString() + " %"
    }

    private fun findPercentile(percentile: Int, results: List<RequestTestResult>) : Int {
        if (results.isEmpty()) {
            return 0
        }

        val sortedTimeResponse = results.map(RequestTestResult::responseTime).sorted()

        return sortedTimeResponse[(percentile * results.size) / 100]
    }
}