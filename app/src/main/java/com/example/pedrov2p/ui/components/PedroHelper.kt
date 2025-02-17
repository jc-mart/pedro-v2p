package com.example.pedrov2p.ui.components

import android.location.Location
import android.net.wifi.rtt.RangingResult
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

const val R = 6371e3 // Earth's radius in meters
const val GPS_ERROR = 1.2 // Derived from PEDRO paper
const val RTT_ERROR = 1.87 // Derived from PEDRO paper

class PedroHelper {
    fun haversineDistance(lat1: Double, long1: Double, lat2: Double, long2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLong = Math.toRadians(long2 - long1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLong / 2) * sin(dLong / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return R * c
    }

    fun movementDetected(
        point1: Pair<RangingResult, Location>,
        point2: Pair<RangingResult, Location>,
        distanceThreshold: Float,
        timeThreshold: Float
    ): Boolean {
        val distance = haversineDistance(
            point1.second.latitude,
            point1.second.longitude,
            point2.second.latitude,
            point2.second.longitude
        )

        val errorMargin = 2 * (GPS_ERROR + RTT_ERROR)
        val minDistance = distance - (
                (point1.first.distanceMm / 1000.0) + (point2.first.distanceMm / 1000.0) -
                        errorMargin
                )
        val timeDifference =
            (point2.first.rangingTimestampMillis - point1.first.rangingTimestampMillis) / 1000.0
        val result = minDistance > distanceThreshold && timeDifference < timeThreshold

        return result
    }
}