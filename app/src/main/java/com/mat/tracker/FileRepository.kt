package com.mat.tracker

import android.content.Context
import android.net.Uri
import android.os.FileObserver
import android.util.Log
import androidx.annotation.MainThread
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.xmlpull.v1.XmlPullParserFactory
import org.xmlpull.v1.XmlSerializer
import java.io.File
import java.io.OutputStreamWriter
import java.lang.Exception
import java.net.URI
import java.util.*
import java.util.Calendar.*

class FileRepository : KoinComponent {

    val newFileEvent: LiveData<Event<String?>>
        get() = _newFileEvent
    val files: LiveData<List<Uri>>
        get() = _files

    private val context: Context by inject()
    private val appDirPath = "${context.filesDir}/"
    private val _newFileEvent: MutableLiveData<Event<String?>> = MutableLiveData()
    private val _files: MutableLiveData<List<Uri>> = MutableLiveData()
    private lateinit var fileObserver: FileObserver

    fun startFileObserver() {
        val appDir = File(appDirPath)
        _files.postValue(extractGpxFilesFromDir(appDir))
        fileObserver = object: FileObserver(appDir) {
            override fun onEvent(event: Int, file: String?) {
                when(event) {
                    CREATE -> {
                        _newFileEvent.postValue(Event(file))
                        _files.postValue(extractGpxFilesFromDir(appDir))
                    }
                    DELETE, MOVED_FROM, MOVED_TO -> {
                        _files.postValue(extractGpxFilesFromDir(appDir))
                    }
                }
            }
        }
        fileObserver.startWatching()
    }

    fun stopFileObserver() {
        fileObserver.stopWatching()
    }

    private fun extractGpxFilesFromDir(dir: File): List<Uri> =
        dir.listFiles()
            .filter {
                it.name.contains(".gpx")
            }
            .sortedByDescending {
                Date(it.lastModified()).time
            }
            .map {
                FileProvider.getUriForFile(context, context.applicationContext.packageName + ".provider", it)
            }

    fun writeLocationsToFile(
        filename: String,
        locations: List<LocationData>
    ): String? {
        val currentDate = Date()
        return writeGpxFile(
            locations,
            filename,
            "",
            "",
            currentDate.time
        )
    }

    fun removeFile(uri: Uri) {
        context.contentResolver.delete(uri, null)
    }

    private fun writeGpxFile(
        locations: List<LocationData>,
        fileName: String,
        description: String,
        authorName: String,
        time: Long
    ): String? {
        var filePath = "${appDirPath}${fileName}.gpx"
        var file = File(filePath)
        if (file.exists()) {
            return fileName
        }
        file.createNewFile()
        val serializer = prepareXmlSerializer(file.writer(), locations, fileName, description, authorName, time)
        serializer.endDocument()
        return null
    }

    private fun prepareXmlSerializer(
        output: OutputStreamWriter,
        locations: List<LocationData>,
        fileName: String,
        description: String,
        authorName: String,
        time: Long
    ): XmlSerializer  {
        val serializer = XmlPullParserFactory.newInstance().newSerializer()
        serializer.setOutput(output)
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
            text(time.toGpxTime())
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
            locations.forEach { it ->
                startTag(null, "trkpt")
                attribute(null, "lat", it.latitude.toString())
                attribute(null, "lon", it.longitude.toString())
                startTag(null, "ele")
                text(it.altitude.toString())
                endTag(null, "ele")
                startTag(null, "time")
                text(it.time.toGpxTime())
                endTag(null, "time")
                endTag(null, "trkpt")
            }
            endTag(null, "trkseg")
            endTag(null, "gpx")
        }
        return serializer
    }
}