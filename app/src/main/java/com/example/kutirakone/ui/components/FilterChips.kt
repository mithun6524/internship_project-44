package com.example.kutirakone.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FilterChips(selectedMaterial: String, onMaterialSelected: (String) -> Unit) {

    val materials = listOf(
        "All", "Silk", "Cotton", "Wool", "Linen", "Denim", 
        "Polyester", "Rayon", "Chiffon", "Georgette", 
        "Velvet", "Satin", "Khadi", "Jute", "Nylon", "Blended"
    )

    Row(modifier = Modifier.horizontalScroll(rememberScrollState()).padding(8.dp)) {
        materials.forEach { material ->
            FilterChip(
                selected = selectedMaterial == material,
                onClick = { onMaterialSelected(material) },
                label = { Text(material) },
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}
