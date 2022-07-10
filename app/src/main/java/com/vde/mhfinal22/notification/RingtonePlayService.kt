package com.vde.mhfinal22.notification

import android.app.Service
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import com.vde.mhfinal22.utils.Define
import com.vde.mhfinal22.utils.L


class RingtonePlayService: Service() {

    private var ringtone: Ringtone? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        L.d("onStartCommand")

        val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        ringtone = RingtoneManager.getRingtone(this, notification)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ringtone?.isLooping = true
        }
        ringtone?.play()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        ringtone?.stop()
    }
}