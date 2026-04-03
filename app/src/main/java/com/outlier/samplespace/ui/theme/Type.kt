package com.outlier.samplespace.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.outlier.samplespace.R

private val DisplayFamily = FontFamily(Font(R.font.kenney_future_narrow))
private val BodyFamily = FontFamily(Font(R.font.kenney_future))

val Typography = Typography(
    headlineLarge = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 38.sp,
        letterSpacing = 0.3.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.2.sp
    ),
    titleLarge = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 23.sp,
        lineHeight = 30.sp
    ),
    titleMedium = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 23.sp
    ),
    bodySmall = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelLarge = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.2.sp
    )
)
