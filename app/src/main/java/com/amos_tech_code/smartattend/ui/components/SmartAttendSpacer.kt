package com.amos_tech_code.smartattend.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
fun SmartAttendSpacer(size: Dp) {
    Spacer(modifier = Modifier.size(size))
}

@Composable
fun SmartAttendHeightSpacer(height: Dp) {
    Spacer(modifier = Modifier.height(height))
}

@Composable
fun SmartAttendWidthSpacer(width: Dp) {
    Spacer(modifier = Modifier.width(width))
}
