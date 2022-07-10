package com.vde.mhfinal22.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.vde.mhfinal22.notification.MyNotificationManager
import com.vde.mhfinal22.notification.RingtonePlayService
import com.vde.mhfinal22.ui.model.ItemNotification
import com.vde.mhfinal22.ui.model.PokemonMH
import com.vde.mhfinal22.ui.model.RepeatInfo
import com.vde.mhfinal22.utils.Define
import com.vde.mhfinal22.utils.L
import com.vde.mhfinal22.utils.MainInterface
import com.vde.mhfinal22.utils.MySP
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val notificationItems = mutableStateListOf<ItemNotification>()
    val alarmStateEnabled = mutableStateOf(false)
    var mainInterface: MainInterface? = null
    var cloudItemNotification: ItemNotification? = null
    var alarmInfo: ItemNotification? = null
    val showDialog = mutableStateOf(false)

    val openAlertDialog = mutableStateOf(false)
    val allPokemonList = mutableStateListOf<PokemonMH>()
    val selectedPokemon = mutableStateListOf<PokemonMH>()
    val rightAnswer = mutableStateOf(PokemonMH())
    var showDialogAfterLoading = false

    var pokeLoading = mutableStateOf(false)


    var selectedNotificationItem: ItemNotification? = null
    var selectedNotificationRepeatInfo = mutableStateListOf<RepeatInfo>()













    private lateinit var textToSpeechEngine: TextToSpeech
    private lateinit var startForResult: ActivityResultLauncher<Intent>

    fun initial(
        engine: TextToSpeech, launcher: ActivityResultLauncher<Intent>
    ) = viewModelScope.launch {
        textToSpeechEngine = engine
        startForResult = launcher
    }

    fun displaySpeechRecognizer() {
        startForResult.launch(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale("en"))
            putExtra(RecognizerIntent.EXTRA_PROMPT, Locale.getDefault())
        })
    }

    fun speak(text: String) = viewModelScope.launch{
        textToSpeechEngine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

























    fun initMainInterface(_mainInterface: MainInterface) {
        mainInterface = _mainInterface
        notificationItems.addAll(MySP.getAlarmList(getApplication()))
    }

    fun addItem(itemNotification: ItemNotification) {
        itemNotification.isEnabled = true
        cloudItemNotification = itemNotification
        checkPermission()
        saveList()
        L.d("addItem itemNotification = $itemNotification")
        mainInterface?.updateWidget()

    }


    fun createNotification() {
        if (cloudItemNotification != null) {
            L.d("createNotification cloudItemNotification = $cloudItemNotification")
            notificationItems.add(cloudItemNotification!!)
            MyNotificationManager().setNotificationAlarm(getApplication(), cloudItemNotification!!)
        }
    }

    fun stopService(context: Context) {
        if (alarmInfo != null) {
            MyNotificationManager().clearNotification(context, alarmInfo!!)
        }
        val stopIntent = Intent(context, RingtonePlayService::class.java)
        context.stopService(stopIntent)
        alarmStateEnabled.value = false
    }


    val permissionCheck = ContextCompat.checkSelfPermission(getApplication(), Define.PERMISSION)

    fun checkPermission() {
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            mainInterface?.requestPermission()
        } else {
            createNotification()
        }
    }


    fun saveList() {
        L.d("saveList")
        val json = Gson().toJson(ArrayList(notificationItems))
        L.d("saveList json = $json")
        MySP.setStringValue(getApplication(), json, Define.ALARM_LIST)

    }

    fun removeItem() {
        L.d("removeItem")
        if (selectedNotificationItem != null) {

            val time = selectedNotificationItem?.calendar?.timeInMillis
            L.d("removeItem time = $time")
            for (item in notificationItems) {
                L.d("item time = ${item.calendar.timeInMillis}")
                if (item.calendar.timeInMillis == time) {
                    MyNotificationManager().deleteAlarm(getApplication(), item)
                    notificationItems.remove(item)
                    showDialog.value = false
                    break
                }
            }
            saveList()
            mainInterface?.updateWidget()

        }
    }

    fun preparePokemonList(pokeList: ArrayList<PokemonMH>) {
        pokeLoading.value = false
        allPokemonList.addAll(pokeList)
        if(showDialogAfterLoading){
            prepareAlertDialogList()
            showDialogAfterLoading = false
        }
    }

    fun prepareAlertDialogList(){
        if(allPokemonList.isNotEmpty()) {
            val newLIst = ArrayList(allPokemonList)
            newLIst.shuffle()
            selectedPokemon.clear()
            for (i in 0 until 4) {
                val pokemonMH = newLIst[i]
                L.d("pokemonMH = $pokemonMH")
                selectedPokemon.add(pokemonMH)
            }
            val number: Int = (0..3).random()
            L.d("random number = $number")

            rightAnswer.value = selectedPokemon[number]
            L.d("rightAnswer pokemon = ${selectedPokemon[number]}")
            openAlertDialog.value = true
        }else{
            showDialogAfterLoading = true
        }
    }


    fun pokemonClick(pokemon: PokemonMH){
        if(pokemon.name == rightAnswer.value.name){
            openAlertDialog.value = false
            stopService(getApplication())
        }else{
            prepareAlertDialogList()
        }
        L.d("pokemonClick pokemon = ${pokemon.name}")
        L.d("pokemonClick rightAnswer = ${rightAnswer.value.name}")
    }
}