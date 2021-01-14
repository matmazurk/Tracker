package com.mat.tracker

import android.location.Location
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class LocationData(
    @PrimaryKey val time: Long,
    val provider: String,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val accuracy: Float
)

fun Location.toLocationData() =
    LocationData(
        provider = "fused",
        time = this.time,
        latitude = this.latitude,
        longitude = this.longitude,
        altitude = this.altitude,
        accuracy = this.accuracy
    )
