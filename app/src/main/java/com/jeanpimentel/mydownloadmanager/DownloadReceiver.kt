package com.jeanpimentel.mydownloadmanager

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class DownloadReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0)
        if (id !in getDownloads(context)) {
            return
        }

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        val query = DownloadManager.Query()
        query.setFilterById(id)

        val cursor = downloadManager.query(query)
        if (cursor.moveToFirst()) {

            val columnStatus = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            val status = cursor.getInt(columnStatus)

            var statusText = ""
            when (status) {
                DownloadManager.STATUS_FAILED -> statusText = "STATUS_FAILED"
                DownloadManager.STATUS_PAUSED -> statusText = "STATUS_PAUSED"
                DownloadManager.STATUS_PENDING -> statusText = "STATUS_PENDING"
                DownloadManager.STATUS_RUNNING -> statusText = "STATUS_RUNNING"
                DownloadManager.STATUS_SUCCESSFUL -> statusText = "STATUS_SUCCESSFUL"
            }

            Toast.makeText(context, "$id: $statusText", Toast.LENGTH_SHORT).show()

            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                removeDownload(context, id)
            }
        } else {

            Toast.makeText(context, "$id: STATUS_CANCELLED", Toast.LENGTH_SHORT).show()

            // Cancelled
            removeDownload(context, id)
        }

    }
}
