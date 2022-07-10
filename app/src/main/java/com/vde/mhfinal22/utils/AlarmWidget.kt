package com.vde.mhfinal22.utils

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Typeface
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.layout.*
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vde.mhfinal22.R
import com.vde.mhfinal22.notification.MyNotificationManager
import com.vde.mhfinal22.ui.model.ItemNotification
import com.vde.mhfinal22.ui.model.getTextForRepeatInfo
import com.vde.mhfinal22.ui.model.getTimeInfo
import com.vde.mhfinal22.ui.theme.RED
import com.vde.mhfinal22.ui.theme.Typography
import com.vde.mhfinal22.ui.theme.Yellow
import java.util.*
import java.util.prefs.Preferences
import kotlin.collections.ArrayList


class AlarmWidget : GlanceAppWidget() {
    override var stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    @Composable
    override fun Content() {
        L.d("AlarmWidget")
        val context = LocalContext.current
        val gsonList = MySP.getStringValue(context, Define.ALARM_LIST)
        val allInfo: ArrayList<ItemNotification> =
            (Gson().fromJson(gsonList, object : TypeToken<ArrayList<ItemNotification>>() {}.type))

        val modifier = GlanceModifier
            .fillMaxSize()
            .background(
                day = Color.White,
                night = RED
            )
            .appWidgetBackground()
            .cornerRadius(16.dp)
            .padding(8.dp)
        AlarmWidgetContent(modifier = modifier, allInfo)
    }
}

class AlarmWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AlarmWidget()
}



@Composable
fun AlarmWidgetContent(
    modifier: GlanceModifier,
    allInfo: ArrayList<ItemNotification>
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val context = LocalContext.current
        var empty = true
        if(allInfo.isNotEmpty()){
            for(item in allInfo){
                if(item.isEnabled){
                    empty = false
                }
                break
            }
        }
        if (empty) {
            L.d("allInfo isEmpty")
            PickachuWidget(
                context, modifier = GlanceModifier
                    .fillMaxWidth()
                    .defaultWeight()
            )
        } else {
            L.d("allInfo isNot Empty size = ${allInfo.size}")
            allInfo.sortBy { it.calendar.timeInMillis }

            for (item in allInfo) {
                L.d("item = ${item.isEnabled}, ${item.calendar.timeInMillis}")
                if (item.calendar.timeInMillis > Calendar.getInstance().timeInMillis
                    && item.isEnabled
                ) {
                    ItemNotifyWidget(
                        context, itemNotification = item, modifier = GlanceModifier
                            .fillMaxWidth()
                            .defaultWeight()
                    )
                    break
                }
            }

        }
    }
}


class ClearWaterClickAction : ActionCallback {
    override suspend fun onRun(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        /*updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) {
            it.toMutablePreferences()
                .apply {
                    //this[intPreferencesKey(WATER_WIDGET_PREFS_KEY)] = 0
                }
        }*/
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) {
            it.toMutablePreferences()
                .apply {
                    val gsonList = MySP.getStringValue(context, Define.ALARM_LIST)
                    val allInfo: ArrayList<ItemNotification> =
                        (Gson().fromJson(gsonList, object : TypeToken<ArrayList<ItemNotification>>() {}.type))
                    allInfo.sortBy { it.calendar.timeInMillis }

                    for (item in allInfo) {
                        L.d("item = ${item.isEnabled}, ${item.calendar.timeInMillis}")
                        if (item.calendar.timeInMillis > Calendar.getInstance().timeInMillis
                            && item.isEnabled
                        ) {
                            L.d("change alarm list item = $item")
                            item.isEnabled = false
                            MyNotificationManager().deleteAlarm(context, item)
                            break
                        }
                    }
                    MySP.setStringValue(context, Gson().toJson(allInfo), Define.ALARM_LIST)

                    /*val glassesOfWater = this[intPreferencesKey(WATER_WIDGET_PREFS_KEY)] ?: 0
                    if (glassesOfWater < MAX_GLASSES) {
                        this[intPreferencesKey(WATER_WIDGET_PREFS_KEY)] = glassesOfWater + 1
                    }*/
                }
        }
        AlarmWidget().update(context, glanceId)
    }
}


@Composable
fun PickachuWidget(
    context: Context,
    modifier: GlanceModifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            provider = ImageProvider(
                resId = R.mipmap.picachu
            ),
            contentDescription = "Pikachu",
            modifier = GlanceModifier
                .defaultWeight()
                .padding(top = 16.dp)
        )


        Text(
            text = "У вас пока нет будильников",
            modifier = GlanceModifier
                .clickable(
                    onClick = actionRunCallback<ClearWaterClickAction>()
                )
                .defaultWeight()
                .padding(top = 4.dp, start = 24.dp, end = 24.dp, bottom = 16.dp),
            style = TextStyle(color = ColorProvider(R.color.black), 14.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center  )

            )


    }
}

@Composable
fun ItemNotifyWidget(
    context: Context,
    itemNotification: ItemNotification,
    modifier: GlanceModifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically,
    ){
            val txt = getTimeInfo(itemNotification.calendar)
            Text(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .padding(4.dp),
                text = txt,
                style = TextStyle(color = ColorProvider(R.color.black), 32.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Start  )
            )

        Row(modifier = modifier.fillMaxWidth().padding(top = 24.dp, start = 4.dp, end = 4.dp, bottom = 8.dp)){
            Text(
                text = "Ближайший будильник",
                style = TextStyle(color = ColorProvider(R.color.black), 14.sp, fontWeight = FontWeight.Normal, textAlign = TextAlign.Start  )
            )

            Text(
                modifier = GlanceModifier
                    .clickable(
                        onClick = actionRunCallback<ClearWaterClickAction>()
                    )
                    .fillMaxWidth()
                    .padding(4.dp),
                text = "СТОП",
                style = TextStyle(color = ColorProvider(RED), 32.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.End  )
            )
        }

    }
}




