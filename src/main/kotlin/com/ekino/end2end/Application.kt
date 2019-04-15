package com.ekino.end2end

import com.ekino.end2end.dto.ResultOrigin
import com.ekino.end2end.dto.ResultType
import com.ekino.end2end.service.ResultsAnalyzerManager
import com.ekino.end2end.service.ResultsReader

class Application {

    companion object {
        @JvmStatic
        fun main(args : Array<String>) {
            val reader = ResultsReader()

            ResultType.values().forEach {
                type ->
                run {
                    val results = reader.readResults(type, ResultOrigin.ACTUAL)
                    val references = reader.readResults(type, ResultOrigin.REFERENCE)
                    ResultsAnalyzerManager.getAnalyzer(type).validate(results, references)
                    val parsedResults = ResultsAnalyzerManager.getAnalyzer(type).parse(results)
                    // TODO upload the results into your favorite storage service
                }
            }
        }
    }
}