package com.ekino.end2end.service

import com.ekino.end2end.dto.ITestResult

interface IRequestsAnalyzer {

    fun parse(results: List<ITestResult>) : List<List<Any>>

    fun validate(results: List<ITestResult>, references: List<ITestResult>)

}