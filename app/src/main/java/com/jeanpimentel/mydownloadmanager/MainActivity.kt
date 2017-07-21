package com.jeanpimentel.mydownloadmanager

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
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
            download("Large", null, largeURL + noCache)
        }

        downloadSmallImage.setOnClickListener {
            download("Small", null, smallURL + noCache)
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

    private fun download(title: String, description: String?, uri: String) {

        val request = DownloadManager.Request(Uri.parse(uri))

        val appName = getString(R.string.app_name)
        request.setTitle("$appName: $title")

        description?.let {
            request.setDescription(description)
        }

        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)

        request.setVisibleInDownloadsUi(false)

        val id = downloadManager?.enqueue(request) ?: -1L
        if (id == -1L) {
            return
        }

        DownloadManagerHelper.saveDownload(this, id)

        checkDownload(id)
    }

    private fun checkDownload(id: Long) {
        downloadManager?.let {

            val file = DownloadManagerHelper.getDownloadedFile(this, id)

            monitor.append(file.toString() + "\n\n")

            if (id in DownloadManagerHelper.getDownloads(this)) {
                Handler().postDelayed({ checkDownload(id) }, 500)
            }
        }
    }

}
