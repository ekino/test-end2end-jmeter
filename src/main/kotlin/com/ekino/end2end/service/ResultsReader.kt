package com.ekino.end2end.service

import com.ekino.end2end.dto.ITestResult
import com.ekino.end2end.dto.RequestTestResult
import com.ekino.end2end.dto.ResultType
import com.ekino.end2end.dto.VMTestResult
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException

class ResultsReader {

    companion object {
        private val RESULTS_FILENAME_PER_TYPE = mapOf(
                Pair(ResultType.REQUEST, "scripts/jmeter_output.csv"),
                Pair(ResultType.VM, "scripts/vm_output.csv"))
        private val CSV_SEPARATOR_PER_TYPE = mapOf(
                Pair(ResultType.REQUEST, ","),
                Pair(ResultType.VM, ";"))
        private val RESULT_CLASS_PER_TYPE : Map <ResultType, (List<String>) -> ITestResult> = mapOf(
                Pair(ResultType.REQUEST, { columns -> RequestTestResult(columns) }),
                Pair(ResultType.VM, { columns -> VMTestResult(columns) })
        )
    }

    fun readResults(type: ResultType) : List<ITestResult> {
        var fileReader: BufferedReader? = null

        val results = ArrayList<ITestResult>()
        try {

            fileReader = BufferedReader(FileReader(RESULTS_FILENAME_PER_TYPE[type]))

            var line = fileReader.readLine()
            while (line != null) {
                val tokens = line.split(CSV_SEPARATOR_PER_TYPE[type]!!)
                if (tokens.isNotEmpty()) {
                    val result = RESULT_CLASS_PER_TYPE[type]!!.invoke(tokens)
                    results.add(result)
                }
                line = fileReader.readLine()
            }

        } catch (e: Exception) {
            println("Error while reading the file ${RESULTS_FILENAME_PER_TYPE[type]}")
            e.printStackTrace()
            System.exit(4)
        } finally {
            try {
                fileReader!!.close()
            } catch (e: IOException) {
                println("Error while closing the stream for file ${RESULTS_FILENAME_PER_TYPE[type]}")
                e.printStackTrace()
                System.exit(5)
            }
        }
        return results
    }
}