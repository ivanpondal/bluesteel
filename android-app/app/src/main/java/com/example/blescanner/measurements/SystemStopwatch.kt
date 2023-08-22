package com.example.blescanner.measurements

class SystemStopwatch : Stopwatch {
    var startTime: Long = 0
    override fun start() {
        startTime = System.currentTimeMillis()
    }

    override fun stop(): Long {
        return System.currentTimeMillis() - startTime
    }
}