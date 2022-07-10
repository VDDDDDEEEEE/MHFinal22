package com.vde.mhfinal22.notification

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.gson.Gson
import com.vde.mhfinal22.ui.model.ItemNotification
import com.vde.mhfinal22.ui.model.getNotificationId
import com.vde.mhfinal22.utils.Define
import com.vde.mhfinal22.utils.Define.my_flag
import com.vde.mhfinal22.utils.L
import java.text.SimpleDateFormat
import java.util.*


class MyNotificationManager {

    fun setNotificationAlarm(context: Context, item: ItemNotification){
        try {
            val alarmManager =
                context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, MyReceiver::class.java)
            val dateWithTime = SimpleDateFormat("yyyy-MM-dd HH:mm").format(item.calendar.time)
            val id = getNotificationId(item.calendar)
            intent.action = dateWithTime
            intent.putExtra(Define.ITEM_NOTIFICATION, Gson().toJson(item))
            intent.putExtra(Define.NOTIFICATION_ID, id)

            val pendingIntent =
                PendingIntent.getBroadcast(context, 1, intent, my_flag)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, item.calendar.timeInMillis, pendingIntent)
            L.d("myLog alarm on ${intent.action}")
            L.d("myLog id ${id}")

        }catch (e:Exception){
            L.d("setAlarm Error = ${e.localizedMessage}")
            e.printStackTrace()
        }
    }

    fun deleteAlarm(context: Context, item: ItemNotification) {
        try {
            val dateWithTime = SimpleDateFormat("yyyy-MM-dd HH:mm").format(item.calendar.time)
            val alarmManager =
                context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, MyReceiver::class.java)
            val id = getNotificationId(item.calendar)
            intent.action = dateWithTime
            intent.putExtra(Define.ITEM_NOTIFICATION, Gson().toJson(item))
            intent.putExtra(Define.NOTIFICATION_ID, id)

            val pendingIntent =
                PendingIntent.getBroadcast(context, 1, intent, my_flag)
            alarmManager.cancel(pendingIntent)
            L.d("myLog deleteAlarm intent action ${intent.action} ")
        } catch (e: Exception) {
            L.d("myLog deleteAlarm alarm error ${e.localizedMessage} ")
        }

        /*
        L.d("myLog ${context.getSystemService(AlarmManager::class.java).nextAlarmClock.triggerTime}")
        Toast.makeText(context,"myLog ${context.getSystemService(AlarmManager::class.java).nextAlarmClock.triggerTime}",Toast.LENGTH_SHORT).show()*/
    }

    fun clearNotification(context: Context, item: ItemNotification) {
        val id = getNotificationId(item.calendar)
        L.d("clearNotification iid= $id")
        val notificationManager = context
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(id)
    }


}