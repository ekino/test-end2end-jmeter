package com.ekino.end2end.service

import com.ekino.end2end.dto.ResultType

class ResultsAnalyzerManager {

    companion object {
        private val ANALYZERS_PER_TYPE = mapOf(
                Pair(ResultType.REQUEST, RequestsResultsAnalyzer()),
                Pair(ResultType.VM, VMResultsAnalyzer())
            )

        fun getAnalyzer(type: ResultType) : IRequestsAnalyzer {
            return ANALYZERS_PER_TYPE[type]!!
        }

    }
}