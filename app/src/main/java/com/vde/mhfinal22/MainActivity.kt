package com.vde.mhfinal22

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Dialog
import android.app.TimePickerDialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.gson.Gson
import com.skydoves.landscapist.glide.GlideImage
import com.vde.mhfinal22.ui.DialogPokemon
import com.vde.mhfinal22.ui.compose.*
import com.vde.mhfinal22.ui.model.*
import com.vde.mhfinal22.ui.theme.*
import com.vde.mhfinal22.ui.viewmodel.MainViewModel
import com.vde.mhfinal22.utils.*
import com.vde.mhfinal22.utils.Define.PERMISSION
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import me.sargunvohra.lib.pokekotlin.client.ClientConfig
import me.sargunvohra.lib.pokekotlin.client.PokeApiClient
import org.intellij.lang.annotations.Language
import java.text.SimpleDateFormat
import java.time.format.TextStyle
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : ComponentActivity(), MainInterface {
    val viewModel: MainViewModel by viewModels()


    companion object {
        private const val REQUEST_CODE_STT = 1
    }

    private val textToSpeechEngine: TextToSpeech by lazy {
        TextToSpeech(this,
            TextToSpeech.OnInitListener { status ->
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeechEngine.language = Locale.UK
                }
            })
    }
    private val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val spokenText: String? =
                result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    .let { text -> text?.get(0) }
            L.d("spokenText = $spokenText")
            if (spokenText.equals("стоп", true)) {
                viewModel.stopService(this)
                viewModel.openAlertDialog.value = false
            } else {

                try {
                    val formatter = SimpleDateFormat("HHmm")
                    val number = spokenText?.filter { it.isDigit() }
                    val time = formatter.parse(number)
                    L.d("time = $time")
                    val cal = Calendar.getInstance()
                    cal.set(Calendar.HOUR_OF_DAY, time.hours)
                    cal.set(Calendar.MINUTE, time.minutes)
                    L.d("time cal= ${cal.get(Calendar.HOUR_OF_DAY)}")
                    L.d("time = ${cal.get(Calendar.MINUTE)}")
                    viewModel.addItem(
                        ItemNotification(
                            cal,
                            isEnabled = true,
                            createRegularAlarmDayOfWeekList()
                        )
                    )
                } catch (e: Exception) {
                    L.d("error = ${e.localizedMessage}")
                }
            }

        }
    }


    @SuppressLint("CoroutineCreationDuringComposition")
    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.initMainInterface(this)
        viewModel.initial(textToSpeechEngine, startForResult)
        //WindowCompat.setDecorFitsSystemWindows(window, false)
        val isAlarmEnabled = intent.getBooleanExtra(Define.ALARM_ENABLED, false)

        val alInfo = Gson().fromJson(
            intent.getStringExtra(Define.ITEM_NOTIFICATION),
            ItemNotification::class.java
        )
        L.d("alInfo = ${alInfo?.calendar?.timeInMillis}")
        viewModel.alarmInfo = alInfo
        viewModel.alarmStateEnabled.value = isAlarmEnabled
        if (isAlarmEnabled) {
            viewModel.prepareAlertDialogList()
        }
        setContent {
            val sheetstate = rememberBottomSheetState(initialValue = BottomSheetValue.Collapsed)
            val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetstate)
            val scope = rememberCoroutineScope()

            val pokesheetstate = rememberBottomSheetState(initialValue = BottomSheetValue.Collapsed)
            val pokescaffoldState =
                rememberBottomSheetScaffoldState(bottomSheetState = pokesheetstate)
            val pokescope = rememberCoroutineScope()
            MHFinal22Theme {
                /*val systemUiController = rememberSystemUiController()
                val useDarkIcons = isSystemInDarkTheme()
                SideEffect {
                    systemUiController.setSystemBarsColor(Yellow, darkIcons = true)
                }*/
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Yellow
                ) {
                    Box() {
                        /*if(viewModel.pokeLoading.value){

                        }else{

                        }*/
                        Column(
                            Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            //verticalArrangement = Arrangement.Center
                        ) {
                            Title()
                            Pickachu(viewModel)
                            SetItemList(viewModel)


                        }
                        FloatBtn(this@MainActivity, viewModel)
                        FloatingVoiceBtn(this@MainActivity, viewModel)
                        if (viewModel.showDialog.value) {
                            BottomDialog(sheetstate, scaffoldState, scope, viewModel)
                        }
                        if (viewModel.openAlertDialog.value) {
                            DialogPokemon(pokesheetstate, pokescaffoldState, pokescope, viewModel)
                        }

                    }
                    if (viewModel.showDialog.value) {
                        scope.launch {
                            if (sheetstate.isCollapsed) {
                                sheetstate.expand()
                            }
                        }
                    }
                    if (viewModel.openAlertDialog.value) {
                        L.d("openALerDialog true")
                        scope.launch {
                            if (pokesheetstate.isCollapsed) {
                                pokesheetstate.expand()
                            }
                        }

                    }
                }
            }

        }
        if (viewModel.allPokemonList.isEmpty()) {


            GlobalScope.launch {
                openPoke()
            }
        }


    }

    override fun onResume() {
        MySP.setBooleanValue(this, Define.IS_APP_ENABLED, true)
        super.onResume()
    }

    override fun onPause() {

        MySP.setBooleanValue(this, Define.IS_APP_ENABLED, false)
        viewModel.saveList()
        textToSpeechEngine.stop()
        super.onPause()
    }

    override fun onDestroy() {

        textToSpeechEngine.shutdown()
        super.onDestroy()
    }


    override fun requestPermission() {
        requestPermissionLauncher.launch(PERMISSION)
    }


    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                viewModel.createNotification()
            } else {
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
            }
        }


    override fun updateWidget() {
        try {
            L.d("Main Update Widget")
            val widgetIntent = Intent(this, AlarmWidgetReceiver::class.java)
            widgetIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val ids: IntArray = AppWidgetManager.getInstance(application)
                .getAppWidgetIds(ComponentName(application, AlarmWidgetReceiver::class.java))
            widgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            sendBroadcast(widgetIntent)
        } catch (e: Exception) {
            L.d("Main Update e = ${e.localizedMessage}")
            e.printStackTrace()
        }
    }

    fun openPoke() {
        viewModel.pokeLoading.value = true
        val pokeApi = PokeApiClient()
        val list = pokeApi.getPokemonList(0, 10).results
        val pokeList = ArrayList<PokemonMH>()
        for (item in ArrayList(list)) {
            val pokemon = pokeApi.getPokemon(item.id)
            val pokemonUri =
                "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${item.id}.png"
            pokeList.add(PokemonMH(pokemon.name, pokemonUri, pokemon.id))
            L.d("pokemon name = ${pokemon.name} image = ${pokemonUri}")
        }
        viewModel.preparePokemonList(pokeList)
    }


}


