package com.ekino.end2end.dto

data class VMTestResult(val columns: List<String>) : ITestResult {
    val serviceName = columns[0].substring(0, columns[0].lastIndexOf("_"))
    val metricName = columns[0].substring(columns[0].lastIndexOf("_") + 1)
    val value : Double = columns[2].toDouble()
}