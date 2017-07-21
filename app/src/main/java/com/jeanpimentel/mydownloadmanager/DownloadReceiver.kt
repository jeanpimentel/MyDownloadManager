package com.jeanpimentel.mydownloadmanager

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class DownloadReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0)
        if (id !in DownloadManagerHelper.getDownloads(context)) {
            return
        }

        val file = DownloadManagerHelper.getDownloadedFile(context, id)

        Toast.makeText(context, "$id: ${file.statusText}", Toast.LENGTH_SHORT).show()

        DownloadManagerHelper.removeDownload(context, id)

        if (file.isSuccessful()) {
            context.startService(DownloadIntentService.create(context, file))
        }
    }
}
