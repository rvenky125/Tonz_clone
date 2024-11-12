package com.famas.tonz.core.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

@Composable
fun Float.toDp(): Dp = with(LocalDensity.current) { toDp() }

@Composable
fun Dp.toPx(): Float = with(LocalDensity.current) { toPx() }