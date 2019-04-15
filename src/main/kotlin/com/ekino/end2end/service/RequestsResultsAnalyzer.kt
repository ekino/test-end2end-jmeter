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

        private val PERCENTILES = listOf(80, 90, 95)
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

    override fun validate(results: List<ITestResult>, references: List<ITestResult>) {
        // grouping the results per components
        val parsedResults = parse(results).groupBy { entry -> entry[1] }
        val parsedReferences = parse(references).groupBy { entry -> entry[1] }

        COMPONENTS.forEach{
            (name, predicate) ->
            run {
                assert(fromPercentage(parsedResults[name]!![0][3].toString()) <= fromPercentage(parsedReferences[name]!![0][3].toString())) { "Error percentage too high for $name" }

                PERCENTILES.forEachIndexed{
                    index, percentile -> assert(parsedResults[name]!![0][index + 4].toString().toInt() <= parsedReferences[name]!![0][index + 4].toString().toInt()) { "$percentile percentile response time too high for $name" }
                }
            }
        }
    }

    private fun fromPercentage(percentage: String) : Float {
        return "(\\d) %".toRegex().find(percentage)!!.groupValues[1].toFloat()
    }

    private fun parseSingleComponent(component: String, date: String, results: List<RequestTestResult>) : List<Any> {
        val singleLine = mutableListOf(
                date,
                component,
                countRequests(results),
                errorPercentage(results)
        )

        PERCENTILES.forEach{
            percentile -> singleLine.add(findPercentile(percentile, results))
        }

        return singleLine
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