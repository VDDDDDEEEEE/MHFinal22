package com.vde.mhfinal22.notification

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.vde.mhfinal22.MainActivity
import com.vde.mhfinal22.R
import com.vde.mhfinal22.ui.model.ItemNotification
import com.vde.mhfinal22.ui.model.getTimeInfo
import com.vde.mhfinal22.utils.Define
import com.vde.mhfinal22.utils.Define.CHANNEL_ID
import com.vde.mhfinal22.utils.Define.my_flag
import com.vde.mhfinal22.utils.L
import com.vde.mhfinal22.utils.MySP


class MyReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        L.d("onReceive")
        val itemGson = intent.getStringExtra(Define.ITEM_NOTIFICATION)
        val item = Gson().fromJson(itemGson, ItemNotification::class.java)
        val name = "Vezdekod"
        val descriptionText = "Vezdekod task notification"
        val mChannel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH)
        mChannel.description = descriptionText
        mChannel.lockscreenVisibility = Notification.DEFAULT_ALL
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)

        val mainIntent = Intent(context, MainActivity::class.java)
        mainIntent.putExtra(Define.ALARM_ENABLED, true)
        mainIntent.putExtra(Define.ITEM_NOTIFICATION, itemGson)
        val mBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("ВСТАВАЙ, ВЕЗДЕКОДЕР!")
            .setContentText("Уже ${getTimeInfo(item.calendar)}")
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.picachu))
            .setAutoCancel(true)
            .setSilent(true)
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    0,
                    mainIntent,
                    my_flag
                )
            )

        mBuilder.setSmallIcon(R.drawable.ic_notification)
        mBuilder.color = ContextCompat.getColor(context, R.color.yellow)

        //val am = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Generate an Id for each notification
        val id = intent.getIntExtra(Define.NOTIFICATION_ID, 100)

        // Show a notification
        notificationManager.notify(id, mBuilder.build())
        startAlarmRington(context)


        if (MySP.getBooleanValue(context, Define.IS_APP_ENABLED)) {
            mainIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(mainIntent)
        }else{
            lightUp(context)
        }
    }

    private fun startAlarmRington(context: Context) {
        val ringtoneIntent = Intent(context, RingtonePlayService::class.java)
        context.startService(ringtoneIntent)

    }

    fun lightUp(context: Context){
        L.d("lightUp")
        val pm: PowerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val isScreenOn = pm.isScreenOn
        L.e("screen on........ $isScreenOn")
        if (!isScreenOn) {
            val wl = pm.newWakeLock(
                PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
                "myApp:MyLock"
            )
            wl.acquire(10000)
            val wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "myApp:mycpuMyCpuLock")
            wl_cpu.acquire(10000)
        }
    }
}