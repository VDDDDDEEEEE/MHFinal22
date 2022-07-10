package com.vde.mhfinal22.ui.model

import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.*
import kotlin.collections.ArrayList

data class ItemNotification(
    var calendar: Calendar,
    var isEnabled: Boolean,
    var repeatList: ArrayList<RepeatInfo>
)

data class RepeatInfo(var isEnabled: Boolean, var dayOfWeek: DayOfWeek)

fun getTimeInfo(calendar: Calendar?): String {
    if (calendar != null) {
        val hour = String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY))
        val minute = String.format("%02d", calendar.get(Calendar.MINUTE))

        return "$hour:$minute"
    } else {
        return "4:20"
    }
}


fun getNotificationId(calendar: Calendar?): Int {
    if (calendar != null) {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        return ("$year$month$hour$minute").toInt()
    } else {
        return 322
    }
}

fun isRepeat(item: ItemNotification): Boolean {
    return (item.repeatList.any { it.isEnabled } || item.repeatList.all { it.isEnabled })
}

fun getTextForRepeatInfo(item: ItemNotification): String {
    var str = ""
    if (item.repeatList.all { it.isEnabled }) {
        str = "Каждый день"
    } else if (item.repeatList.all { !it.isEnabled }) {
        str = "Сегодня"
    } else {
        for (dayInfo in item.repeatList) {
            if (dayInfo.isEnabled) {
                str += " ${dayInfo.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())}"
            }
        }
    }
    return str
}

fun createDayOfWeekList(): ArrayList<RepeatInfo> {
    val list = ArrayList<RepeatInfo>()
    for (i in 1 until 8) {
        list.add(RepeatInfo(false, DayOfWeek.of(i)))
    }
    return list
}