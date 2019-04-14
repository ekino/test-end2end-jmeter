package com.ekino.end2end

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
                    val results = reader.readResults(type)
                    val parsedResults = ResultsAnalyzerManager.getAnalyzer(type).parse(results)
                    // TODO upload the results into your favorite storage service
                }
            }
        }
    }
}