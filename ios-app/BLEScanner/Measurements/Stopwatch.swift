//
//  Stopwatch.swift
//  BLEScanner
//
//  Created by iván pondal on 16/05/2023.
//

import Foundation

class Stopwatch {
    let clock: ContinuousClock = ContinuousClock()
    var startTime: ContinuousClock.Instant = .now
    var elapsedTime: Duration = .zero

    func start() {
        startTime = clock.now
    }

    func stop() -> Duration {
        elapsedTime = startTime.duration(to: clock.now)
        return elapsedTime
    }
}
