package com.ekino.end2end.dto

data class RequestTestResult(val columns: List<String>) : ITestResult {
    val responseTime = columns[1].toInt()
    val responseCode = columns[2]
    val responseStatus = columns[5]
    val requestUrl = columns[7]
}