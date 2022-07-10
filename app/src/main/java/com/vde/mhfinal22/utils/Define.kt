package com.vde.mhfinal22.utils

import android.Manifest
import android.app.PendingIntent
import android.os.Build

object Define {


    const val LOG_ENABLE = true

    val my_flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    } else {
        PendingIntent.FLAG_UPDATE_CURRENT
    }
    const val PERMISSION = Manifest.permission.WAKE_LOCK
    const val REQUEST_READ_PHONE_STATE = 322
    const val ALARM_ENABLED = "ALARM_ENABLED"
    const val CHANNEL_ID = "VEZDEKOD"
    const val ITEM_NOTIFICATION = "ITEM_NOTIFICATION"
    const val NOTIFICATION_ID = "NOTIFICATION_ID"
    const val IS_APP_ENABLED = "IS_APP_ENABLED"
    const val ALARM_LIST = "ALARM_LIST"
}