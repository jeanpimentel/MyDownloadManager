package com.jeanpimentel.mydownloadmanager

import android.app.DownloadManager
import android.app.IntentService
import android.content.Context
import android.content.Intent
import java.io.File
import java.io.FileOutputStream

class DownloadIntentService : IntentService("DownloadIntentService") {

    override fun onHandleIntent(intent: Intent?) {

        if (intent == null) {
            return
        }

        val downloadedFile = intent.getParcelableExtra<DownloadedFile>(DOWNLOAD_BUNDLE)

        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = downloadManager.getUriForDownloadedFile(downloadedFile.id)

        NotificationHelper.showProcessing(this, downloadedFile)

        val inputStream = contentResolver.openInputStream(uri)

        val outputFile = File(filesDir, downloadedFile.id.toString())
        val outputStream = FileOutputStream(outputFile)

        val buf = ByteArray(1024)
        var len: Int = 0

        while ({ len = inputStream.read(buf); len }() > 0) {
            outputStream.write(buf, 0, len)
        }

        outputStream.close()
        inputStream.close()

        downloadManager.remove(downloadedFile.id)

        NotificationHelper.showFinished(this, downloadedFile)
    }

    companion object {

        private val DOWNLOAD_BUNDLE = "${BuildConfig.APPLICATION_ID}.download_bundle"

        fun create(context: Context, file: DownloadedFile): Intent {
            val intent = Intent(context, DownloadIntentService::class.java)
            intent.putExtra(DOWNLOAD_BUNDLE, file)
            return intent
        }
    }
}
