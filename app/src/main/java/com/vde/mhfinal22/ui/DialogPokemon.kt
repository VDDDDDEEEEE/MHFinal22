package com.vde.mhfinal22.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.skydoves.landscapist.glide.GlideImage
import com.vde.mhfinal22.R
import com.vde.mhfinal22.ui.theme.Typography
import com.vde.mhfinal22.ui.theme.WhenDialogOpen
import com.vde.mhfinal22.ui.viewmodel.MainViewModel
import com.vde.mhfinal22.utils.L
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DialogPokemon(
    sheetstate: BottomSheetState,
    scaffoldState: BottomSheetScaffoldState,
    scope: CoroutineScope,
    viewModel: MainViewModel
) {
    L.d("DialogPokemon start")
    val pokemonList = viewModel.selectedPokemon
    val rightAnswerPokemon = viewModel.rightAnswer
    BottomSheetScaffold(

        scaffoldState = scaffoldState,
        sheetContent = {
            Card(
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                L.d("DialogPokemon Card")
                Box(
                    modifier = Modifier
                        .background(Color.White)
                        .fillMaxWidth()
                        .padding(top = 24.dp, bottom = 32.dp, start = 20.dp, end = 20.dp)

                    //.height(300.dp)
                    ,
                    contentAlignment = Alignment.Center
                ) {
                    L.d("DialogPokemon Box")
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        L.d("DialogPokemon Column")
                        Text(
                            modifier = Modifier.height(50.dp),
                            text = "Кликните на ${pokemonList.get(0).name}", style = Typography.h2,
                        )

                        
                        Row(
                            modifier = Modifier.fillMaxWidth(1f),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            L.d("DialogPokemon Row 1")
                            val pokemon1 = pokemonList[0]
                            Image(
                                painter = rememberAsyncImagePainter(pokemon1.image),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxWidth(.5f).
                                clickable {
                                    viewModel.pokemonClick(pokemon1)
                                }
                                ,
                                alignment = Alignment.Center
                            )

                            val pokemon2 = pokemonList[1]
                            Image(
                                painter = rememberAsyncImagePainter(pokemon2.image),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxWidth(1f).
                                clickable {
                                    viewModel.pokemonClick(pokemon2)
                                }
                                ,
                                alignment = Alignment.Center
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(1f),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {

                            val pokemon3 = pokemonList[2]
                            L.d("DialogPokemon Row pokemon3 = $pokemon3")
                            Image(
                                painter = rememberAsyncImagePainter(pokemon3.image),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxWidth(0.5f).
                                clickable {
                                    viewModel.pokemonClick(pokemon3)
                                },
                                alignment = Alignment.Center
                            )
                            val pokemon4 = pokemonList[3]
                            L.d("DialogPokemon Row pokemon4 = $pokemon4")
                            Image(
                                painter = rememberAsyncImagePainter(pokemon4.image),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxWidth(1f).
                                        clickable {
                                                  viewModel.pokemonClick(pokemon4)
                                        }
                                ,
                                alignment = Alignment.Center
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
                        viewModel.openAlertDialog.value = false
                    }
                }),
            contentAlignment = Alignment.Center
        ) {
        }
    }




}

