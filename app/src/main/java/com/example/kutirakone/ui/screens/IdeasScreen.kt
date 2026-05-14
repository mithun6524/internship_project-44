package com.example.kutirakone.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kutirakone.ui.theme.BackgroundSoft

private data class DesignIdea(
    val title: String,
    val category: String,
    val size: String,
    val materials: List<String>,
    val note: String,
    val color: Color
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IdeasScreen() {
    val ideas = listOf(
        DesignIdea("Patchwork tote", "Carry", "0.8-1.2 m", listOf("Denim", "Cotton", "Canvas"), "Mix sturdy scraps for the base and softer pieces for pockets.", Color(0xFF1565C0)),
        DesignIdea("Zip pouch set", "Utility", "0.3-0.5 m", listOf("Silk", "Cotton", "Linen"), "Make three nesting pouches from coordinated small pieces.", Color(0xFFD81B60)),
        DesignIdea("Table runner", "Home", "1.5 m", listOf("Khadi", "Linen", "Jute"), "Use strips in alternating widths for a handwoven look.", Color(0xFF7CB342)),
        DesignIdea("Cushion cover", "Home", "0.7-1 m", listOf("Velvet", "Satin", "Blended"), "Put rich textures on the front and a plain backing behind.", Color(0xFF6A1B9A)),
        DesignIdea("Hair scrunchies", "Accessories", "0.15 m", listOf("Satin", "Chiffon", "Silk"), "Perfect for long narrow leftovers and quick batch making.", Color(0xFF00ACC1)),
        DesignIdea("Baby bib", "Care", "0.35 m", listOf("Cotton", "Flannel", "Terry"), "Layer absorbent cotton with a soft backing.", Color(0xFF43A047)),
        DesignIdea("Bookmark bands", "Gifts", "0.1 m", listOf("Rayon", "Cotton", "Blended"), "Add elastic and a button for a polished small gift.", Color(0xFF7B1FA2)),
        DesignIdea("Wall bunting", "Decor", "0.5 m", listOf("Printed cotton", "Jute", "Linen"), "Cut triangles from colorful leftovers and stitch onto tape.", Color(0xFFEF6C00)),
        DesignIdea("Laptop sleeve", "Protect", "1 m", listOf("Denim", "Wool", "Cotton"), "Quilt the outer layer and add a padded lining.", Color(0xFF546E7A)),
        DesignIdea("Apron pocket panel", "Repair", "0.25 m", listOf("Canvas", "Denim", "Cotton"), "Refresh an old apron with contrast utility pockets.", Color(0xFF8D6E63)),
        DesignIdea("Fabric coasters", "Home", "0.2 m", listOf("Cotton", "Khadi", "Jute"), "Stack layers with scrap batting for firm washable coasters.", Color(0xFF9E7B3F)),
        DesignIdea("Festival potli bag", "Occasion", "0.4 m", listOf("Silk", "Satin", "Georgette"), "Pair shiny fabric with a drawstring and bead trim.", Color(0xFF5E35B1))
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSoft),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFFFFF3E0), Color(0xFFE8F5E9), Color(0xFFE3F2FD))
                            )
                        )
                        .padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.85f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(12.dp)
                                    .size(26.dp)
                            )
                        }
                        Column(modifier = Modifier.padding(start = 14.dp)) {
                            Text(
                                text = "Scrap-to-style ideas",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Pick by fabric size, texture, and what you want to make next.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF455A64)
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = {},
                    label = { Text("${ideas.size} ideas") },
                    leadingIcon = { Icon(Icons.Default.Lightbulb, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                AssistChip(
                    onClick = {},
                    label = { Text("Low waste") },
                    leadingIcon = { Icon(Icons.Default.Recycling, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }
        }

        items(ideas) { idea ->
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = idea.color.copy(alpha = 0.13f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCut,
                                contentDescription = null,
                                tint = idea.color,
                                modifier = Modifier
                                    .padding(10.dp)
                                    .size(22.dp)
                            )
                        }
                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text(
                                text = idea.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${idea.category} | ${idea.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = idea.color,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = idea.note,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF424242)
                    )

                    FlowRow(
                        modifier = Modifier.padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        idea.materials.forEach { material ->
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = idea.color.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = material,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFF263238)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
