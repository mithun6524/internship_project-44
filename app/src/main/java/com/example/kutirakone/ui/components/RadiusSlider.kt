package com.example.kutirakone.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RadiusSlider(radius: Float, onRadiusChanged: (Float) -> Unit) {

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Search Radius: ${radius.toInt()} km")

        Slider(
            value = radius,
            onValueChange = onRadiusChanged,
            valueRange = 1f..10f
        )
    }
}
