package com.msa.compose_kmm.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import compose_kmm.composeapp.generated.resources.Res
import compose_kmm.composeapp.generated.resources.bhoma
import compose_kmm.composeapp.generated.resources.nazanin
import org.jetbrains.compose.resources.Font

@Composable
fun NazaninFontFamily()= FontFamily(Font(Res.font.nazanin))

@Composable
fun BhomaFontFamily()= FontFamily(Font(Res.font.bhoma))