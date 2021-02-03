package com.mat.tracker

import android.location.Location
import android.os.Environment
import android.util.Log
import androidx.annotation.MainThread
import kotlinx.coroutines.*
import org.xmlpull.v1.XmlPullParserFactory
import org.xmlpull.v1.XmlSerializer
import java.io.File
import java.io.StringWriter
import java.util.concurrent.ExecutorService

class Repository(
    private val locationsDao: LocationsDao,
    private val locationManager: LocationManager,
) {

    val receivingLocationUpdates = locationManager.receivingLocationUpdates

    fun getLocations() =
        locationsDao.getAllLocations()

    @Throws(SecurityException::class)
    @MainThread
    fun startTrackingLocation() =
        locationManager.startLocationUpdates()

    @MainThread
    fun stopTrackingLocation() =
        locationManager.stopLocationUpdates()

    suspend fun saveLocation(location: LocationData) =
        locationsDao.insertLocation(location)

    suspend fun saveLocations(locations: List<LocationData>) =
        locationsDao.insertLocations(locations)

    suspend fun clearDatabase() =
        locationsDao.nukeTable()

    fun writeXmlToFile(
            locations: List<LocationData>,
            fileName: String,
            description: String,
            authorName: String,
            time: String
    ) {
        val serializer = prepareXmlSerializer(locations, fileName, description, authorName, time)
        var filePath = "${Environment.getDataDirectory()}${fileName}"
        var file = File(filePath)
        if (file.exists()) {
            filePath = "${filePath}_1"
            file = File(filePath)
        }
        file.createNewFile()
        serializer.setOutput(file.writer())
        serializer.endDocument()
    }

    private fun prepareXmlSerializer(
        locations: List<LocationData>,
        fileName: String,
        description: String,
        authorName: String,
        time: String
    ): XmlSerializer  {
        val serializer = XmlPullParserFactory.newInstance().newSerializer()
        val minLat = locations.minByOrNull { it.latitude }?.latitude ?: 0
        val maxLat = locations.maxByOrNull { it.latitude }?.latitude ?: 0
        val minLon = locations.minByOrNull { it.longitude }?.longitude ?: 0
        val maxLon = locations.maxByOrNull { it.latitude }?.latitude ?: 0
        serializer.run {
            startDocument("UTF-8", true)
            startTag(null, "gpx")
            attribute(null, "version", "1.1")
            attribute(null, "creator", "Tracker App")
            attribute(null, "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance")
            attribute(null, "xmlns", "http://www.topografix.com/GPX/1/1")
            attribute(null, "xsi:schemaLocation", "http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd")
            startTag(null, "metadata")
            startTag(null, "name")
            text(fileName)
            endTag(null, "name")
            startTag(null, "desc")
            text(description)
            endTag(null, "desc")
            startTag(null, "author")
            startTag(null, "name")
            text(authorName)
            endTag(null, "name")
            endTag(null, "author")
            startTag(null, "time")
            text(time)
            endTag(null, "time")
            startTag(null, "bounds")
            attribute(null, "minlat", minLat.toString())
            attribute(null, "maxlat", maxLat.toString())
            attribute(null, "minlon", minLon.toString())
            attribute(null, "maxlon", maxLon.toString())
            endTag(null, "bounds")
            endTag(null, "metadata")
            startTag(null, "src")
            text("Logged by $authorName using Tracker App")
            endTag(null, "src")
            startTag(null, "trkseg")
            locations.forEach {
                startTag(null, "trkpt")
                attribute(null, "lat", it.latitude.toString())
                attribute(null, "lon", it.longitude.toString())
                startTag(null, "ele")
                text(it.altitude.toString())
                endTag(null, "ele")
                startTag(null, "time")
                text(it.time.toString())
                endTag(null, "time")
                endTag(null, "trkpt")
            }
            endTag(null, "trkseg")
            endTag(null, "gpx")
        }
        return serializer
    }

}