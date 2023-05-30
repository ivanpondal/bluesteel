//
//  Stopwatch.swift
//  BLEScanner
//
//  Created by iván pondal on 16/05/2023.
//

import Foundation

protocol Stopwatch {
    mutating func start()
    mutating func stop() -> Int64
}

class DateStopwatch : Stopwatch {
    var startTime: Date = Date()
    var elapsedTime: TimeInterval = .infinity

    func start() {
        startTime = Date()
    }

    func stop() -> Int64 {
        elapsedTime = Date().timeIntervalSince(startTime)
        return .init(elapsedTime*1000)
    }
}

@available(iOS 16, *)
class ContinuousClockStopwatch : Stopwatch {
    let clock: ContinuousClock = ContinuousClock()
    var startTime: ContinuousClock.Instant = .now
    var elapsedTime: Duration = .zero

    func start() {
        startTime = clock.now
    }

    func stop() -> Int64 {
        elapsedTime = startTime.duration(to: clock.now)
        return elapsedTime.ms()
    }
}

@available(iOS 16, *)
extension Duration {
    func ms() -> Int64 {
        let (sec, attosec) = self.components
        return sec*1000 + attosec/(1_000_000_000_000_000)
    }
}
