package com.vde.mhfinal22.ui.compose

import android.app.TimePickerDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vde.mhfinal22.MainActivity
import com.vde.mhfinal22.R
import com.vde.mhfinal22.ui.model.*
import com.vde.mhfinal22.ui.theme.*
import com.vde.mhfinal22.ui.viewmodel.MainViewModel
import com.vde.mhfinal22.utils.L
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.format.TextStyle
import java.util.*


@Composable
fun ShowDialog(viewModel: MainViewModel){
    val openDialog = remember { mutableStateOf(true) }
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = {
            openDialog.value = false
        },
        title = {
            Text(text = "Title")
        },
        text = {
            Column() {
                TextField(
                    value = text,
                    onValueChange = { text = it }
                )
                Text("Custom Text")
                Checkbox(checked = false, onCheckedChange = {})
            }
        },
        buttons = {
            Row(
                modifier = Modifier.padding(all = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { openDialog.value = false }
                ) {
                    Text("Dismiss")
                }
            }
        }
    )
}

@Composable
fun Title() {
    Box(
        contentAlignment = Alignment.TopCenter
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth(1f)
                .padding(top = 32.dp, bottom = 16.dp),
            text = "Будильник",
            style = Typography.body1,
            textAlign = TextAlign.Center

        )
    }
}

@Composable
fun Pickachu(viewModel: MainViewModel) {
    if (!viewModel.alarmStateEnabled.value) {
        if (viewModel.notificationItems.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 200.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(id = R.mipmap.picachu),
                    contentDescription = "Pikachu",
                )
                Text(
                    modifier = Modifier.padding(top = 24.dp),
                    style = Typography.caption,
                    textAlign = TextAlign.Center,
                    text = "У вас пока нет будильников",
                )
            }
        }
    } else {
        Column() {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                text = "Будильник",
                style = Typography.body1,
                textAlign = TextAlign.Center
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = getTimeInfo(viewModel.alarmInfo?.calendar),
                textAlign = TextAlign.Center,
                style = Typography.h1
            )
        }
    }

}

@Composable
fun FloatBtn(
    activity: MainActivity,
    viewModel: MainViewModel
) {
    val state = viewModel.alarmStateEnabled
    Box(
        modifier = Modifier.fillMaxSize()
        ,
        contentAlignment = Alignment.BottomCenter
    ){
        FloatingActionButton(
            onClick = {
                if (state.value) {
                    viewModel.stopService(activity)
                } else {
                    openTimePicker(activity, viewModel)
                }
            },
            modifier = Modifier
                .padding(32.dp)
                .clip(CircleShape),
            backgroundColor = RED,
            elevation = FloatingActionButtonDefaults.elevation(),
        )
        {
            val iconId = if (state.value) R.drawable.ic_baseline_stop_24 else R.drawable.ic_plus
            Icon(
                painter = painterResource(iconId),
                contentDescription = "PLUS",
                tint = Color.White
            )
        }
    }

}

fun openTimePicker(
    activity: MainActivity,
    viewModel: MainViewModel
) {
    L.d("openTimePicker")
    val cal = Calendar.getInstance()
    val timeSetListener =
        TimePickerDialog.OnTimeSetListener { timePickerDialog, hour, minute ->
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            cal.set(Calendar.SECOND, 0)

            viewModel.addItem(ItemNotification(cal, isEnabled = true, createDayOfWeekList()))
        }
    TimePickerDialog(
        activity, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true
    ).show()
}


@Composable
fun SetItemList(viewModel: MainViewModel) {
    val list = viewModel.notificationItems
    L.d("SetItemList list size = ${list}")
    if (!viewModel.alarmStateEnabled.value) {
        val lazyListState: LazyListState = rememberLazyListState()
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            state = lazyListState
        ) {

            itemsIndexed(list) { index, item ->
                L.d("LazyColumn item = $item")
                ItemNotify(itemNotification = item, viewModel)
            }
        }
    }
}


@Composable
fun ItemNotify(itemNotification: ItemNotification, viewModel: MainViewModel) {
    Card(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
    ) {
        Column() {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, top = 10.dp, end = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = getTimeInfo(itemNotification.calendar),
                    style = Typography.h1
                )

                Box(
                    modifier = Modifier
                        //.background(Color.Green)
                        .padding(8.dp)
                        .clickable {
                            viewModel.selectedNotificationRepeatInfo.clear()
                            viewModel.selectedNotificationRepeatInfo.addAll(itemNotification.repeatList)
                            viewModel.selectedNotificationItem = itemNotification
                            viewModel.showDialog.value = true
                        },
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_more_vertical),
                        contentDescription = "More",
                    )
                }

            }

            Box(Modifier.padding(start = 20.dp, bottom = 20.dp, top = 8.dp)) {
                val txt = getTextForRepeatInfo(itemNotification)
                Text(text = txt)
            }

        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BottomDialog(
    sheetstate: BottomSheetState,
    scaffoldState: BottomSheetScaffoldState,
    scope: CoroutineScope,
    viewModel: MainViewModel
) {
    /* val sheetstate = rememberBottomSheetState(initialValue = state)
     val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetstate)
     val scope = rememberCoroutineScope()*/

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            Card(
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(Color.White)
                        .fillMaxWidth()
                        .padding(top = 24.dp, bottom = 32.dp, start = 20.dp, end = 20.dp)

                    //.height(300.dp)
                    ,
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                    ) {
                        Text(
                            modifier = Modifier.height(40.dp),
                            text = "Повторять будильник", style = Typography.h2,
                        )

                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),

                            ) {
                            itemsIndexed(viewModel.selectedNotificationRepeatInfo) { index, item ->
                                MyDayOfWeek(item)
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {


                            Text(
                                modifier = Modifier
                                    .padding(top = 32.dp)
                                    .clickable {
                                        viewModel.removeItem()
                                    },
                                text = "Удалить будильник", style = Typography.body1,
                                color = RED
                            )

                            Text(
                                modifier = Modifier
                                    .padding(top = 32.dp, end = 32.dp)
                                    .clickable {
                                        viewModel.showDialog.value = false
                                    },
                                text = "ОК"
                            )
                        }
                    }
                }
            }
        },
        sheetBackgroundColor = Color.Transparent,
        sheetPeekHeight = 0.dp,
        backgroundColor = Color.Transparent,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(if (sheetstate.isExpanded) 1f else 0f)
                .background(if (sheetstate.isExpanded) WhenDialogOpen else Color.Transparent)
                .clickable(enabled = sheetstate.isExpanded, onClick = {
                    scope.launch {
                        sheetstate.collapse()
                        viewModel.showDialog.value = false
                    }
                }),
            contentAlignment = Alignment.Center
        ) {
        }
    }


}

@Composable
fun MyDayOfWeek(repeatInfo: RepeatInfo) {
    val state = remember { mutableStateOf(repeatInfo.isEnabled) }
    val color = if (state.value) CircleSelected else CircleUnselected
    Column(
        modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Card(
            modifier = Modifier
                .height(40.dp)
                .width(40.dp)
                .background(color, RoundedCornerShape(90)),
            shape = RoundedCornerShape(30.dp),
        ) {
            Box(modifier = Modifier
                .fillMaxSize()
                .background(color, RoundedCornerShape(90))
                .clickable {
                    repeatInfo.isEnabled = !repeatInfo.isEnabled
                    state.value = !state.value
                }
                .clip(RoundedCornerShape(90)),
                contentAlignment = Alignment.Center
            ) {
                Text(

                    text = repeatInfo.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                )
            }

        }
        Icon(
            modifier = Modifier
                .height(12.dp)
                .width(12.dp)
            //.padding(top = 4.dp, bottom = 4.dp)
            ,
            painter = painterResource(R.drawable.ic_check),
            contentDescription = "click",
            tint = if (state.value) Color.Black else Color.Transparent
        )


    }
}


@Composable
fun FloatingVoiceBtn(
    activity: MainActivity,
    viewModel: MainViewModel
) {
    val state = viewModel.alarmStateEnabled
    Box(
        modifier = Modifier.fillMaxSize()
        ,
        contentAlignment = Alignment.BottomEnd
    ){
        FloatingActionButton(
            onClick = {
                viewModel.displaySpeechRecognizer()
            },
            modifier = Modifier
                .padding(32.dp)
                .clip(CircleShape),
            backgroundColor = RED,
            elevation = FloatingActionButtonDefaults.elevation(),
        )
        {
            val iconId = R.drawable.ic_baseline_keyboard_voice_24
            Icon(
                painter = painterResource(iconId),
                contentDescription = "PLUS",
                tint = Color.White
            )
        }
    }

}