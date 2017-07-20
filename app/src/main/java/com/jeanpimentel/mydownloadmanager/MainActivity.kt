package com.jeanpimentel.mydownloadmanager

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.text.method.ScrollingMovementMethod
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val largeURL = "https://upload.wikimedia.org/wikipedia/commons/a/a0/%27Greeley_Panorama%27_from_Opportunity%27s_Fifth_Martian_Winter%2C_PIA15689.jpg"
    private val smallURL = "https://upload.wikimedia.org/wikipedia/commons/d/de/Greeley_opportunity_5000.jpg"

    private val noCache: String
        get() {
            return "?ts=" + System.currentTimeMillis()
        }

    private var downloadManager: DownloadManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        downloadLargeImage.setOnClickListener {
            download("Large", "Large image: 88MB", largeURL + noCache)
        }

        downloadSmallImage.setOnClickListener {
            download("Small", "Small image: 1.1MB", smallURL + noCache)
        }

        viewDownloads.setOnClickListener {
            val i = Intent()
            i.action = DownloadManager.ACTION_VIEW_DOWNLOADS
            startActivity(i)
        }

        monitor.movementMethod = ScrollingMovementMethod()

        monitor.setOnLongClickListener {
            monitor.text = ""
            return@setOnLongClickListener true
        }
    }

    private fun download(title: String, description: String, uri: String) {

        val appName = getString(R.string.app_name)
        val request = DownloadManager.Request(Uri.parse(uri))
        request.setTitle("$appName: $title")
        request.setDescription(description)
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE)

        val id = downloadManager?.enqueue(request) ?: -1L
        if (id == -1L) {
            return
        }

        saveDownload(this, id)

        checkDownload(id)
    }

    private fun checkDownload(id: Long) {
        downloadManager?.let {

            val query = DownloadManager.Query()
            query.setFilterById(id)

            val cursor = it.query(query)
            if (cursor.moveToFirst()) {
                monitor.append(getStatus(cursor, id))
            }

            if (id in getDownloads(this)) {
                Handler().postDelayed({ checkDownload(id) }, 500)
            }

        }
    }

    private fun getStatus(cursor: Cursor, id: Long): String {

        val columnStatus = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
        val status = cursor.getInt(columnStatus)

        val columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
        val reason = cursor.getInt(columnReason)

        var statusText = ""
        var reasonText = ""

        when (status) {
            DownloadManager.STATUS_FAILED -> {
                statusText = "STATUS_FAILED"
                when (reason) {
                    DownloadManager.ERROR_CANNOT_RESUME -> reasonText = "ERROR_CANNOT_RESUME"
                    DownloadManager.ERROR_DEVICE_NOT_FOUND -> reasonText = "ERROR_DEVICE_NOT_FOUND"
                    DownloadManager.ERROR_FILE_ALREADY_EXISTS -> reasonText = "ERROR_FILE_ALREADY_EXISTS"
                    DownloadManager.ERROR_FILE_ERROR -> reasonText = "ERROR_FILE_ERROR"
                    DownloadManager.ERROR_HTTP_DATA_ERROR -> reasonText = "ERROR_HTTP_DATA_ERROR"
                    DownloadManager.ERROR_INSUFFICIENT_SPACE -> reasonText = "ERROR_INSUFFICIENT_SPACE"
                    DownloadManager.ERROR_TOO_MANY_REDIRECTS -> reasonText = "ERROR_TOO_MANY_REDIRECTS"
                    DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> reasonText = "ERROR_UNHANDLED_HTTP_CODE"
                    DownloadManager.ERROR_UNKNOWN -> reasonText = "ERROR_UNKNOWN"
                }
            }
            DownloadManager.STATUS_PAUSED -> {
                statusText = "STATUS_PAUSED"
                when (reason) {
                    DownloadManager.PAUSED_QUEUED_FOR_WIFI -> reasonText = "PAUSED_QUEUED_FOR_WIFI"
                    DownloadManager.PAUSED_UNKNOWN -> reasonText = "PAUSED_UNKNOWN"
                    DownloadManager.PAUSED_WAITING_FOR_NETWORK -> reasonText = "PAUSED_WAITING_FOR_NETWORK"
                    DownloadManager.PAUSED_WAITING_TO_RETRY -> reasonText = "PAUSED_WAITING_TO_RETRY"
                }
            }
            DownloadManager.STATUS_PENDING -> statusText = "STATUS_PENDING"
            DownloadManager.STATUS_RUNNING -> statusText = "STATUS_RUNNING"
            DownloadManager.STATUS_SUCCESSFUL -> statusText = "STATUS_SUCCESSFUL"
        }

        return "$id: $statusText: $reasonText\n\n"
    }

}
