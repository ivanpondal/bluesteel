//
//  Stopwatch.swift
//  BLEScanner
//
//  Created by iván pondal on 16/05/2023.
//

import Foundation

protocol Stopwatch {
    mutating func start()
    mutating func stop()
    mutating func measure<T>(closure: () async throws -> T, onStop: (_ time: Int64) -> Void) async throws -> T
    var lastReading: Int64 { get }
}

class DateStopwatch : Stopwatch {
    var startTime: Date = Date()
    var elapsedTime: TimeInterval = .infinity
    var lastReading: Int64 = 0

    func start() {
        startTime = Date()
    }

    func stop() {
        elapsedTime = Date().timeIntervalSince(startTime)
        lastReading = .init(elapsedTime*1000)
    }

    func measure<T>(closure: () async throws -> T, onStop: (_ time: Int64) -> Void) async throws -> T {
        start()
        let result = try await closure()
        stop()
        onStop(lastReading)
        return result
    }
}

@available(iOS 16, *)
class ContinuousClockStopwatch : Stopwatch {
    let clock: ContinuousClock = ContinuousClock()
    var startTime: ContinuousClock.Instant = .now
    var elapsedTime: Duration = .zero
    var lastReading: Int64 = 0

    func start() {
        startTime = clock.now
    }

    func stop() {
        elapsedTime = startTime.duration(to: clock.now)
        lastReading = elapsedTime.ms()
    }

    func measure<T>(closure: () async throws -> T, onStop: (_ time: Int64) -> Void) async throws -> T {
        start()
        let result = try await closure()
        stop()
        onStop(lastReading)
        return result
    }
}

@available(iOS 16, *)
extension Duration {
    func ms() -> Int64 {
        let (sec, attosec) = self.components
        return sec*1000 + attosec/(1_000_000_000_000_000)
    }
}
