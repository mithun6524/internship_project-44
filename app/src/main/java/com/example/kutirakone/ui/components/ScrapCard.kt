package com.example.kutirakone.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import coil.compose.AsyncImage
import com.example.kutirakone.R
import com.example.kutirakone.model.FabricScrap
import com.example.kutirakone.ui.theme.BlendedColor
import com.example.kutirakone.ui.theme.ChiffonColor
import com.example.kutirakone.ui.theme.CottonColor
import com.example.kutirakone.ui.theme.DenimColor
import com.example.kutirakone.ui.theme.GeorgetteColor
import com.example.kutirakone.ui.theme.JuteColor
import com.example.kutirakone.ui.theme.KhadiColor
import com.example.kutirakone.ui.theme.LinenColor
import com.example.kutirakone.ui.theme.NylonColor
import com.example.kutirakone.ui.theme.PolyesterColor
import com.example.kutirakone.ui.theme.RayonColor
import com.example.kutirakone.ui.theme.SatinColor
import com.example.kutirakone.ui.theme.SilkColor
import com.example.kutirakone.ui.theme.VelvetColor
import com.example.kutirakone.ui.theme.WoolColor

@Composable
fun ScrapCard(scrap: FabricScrap, onClick: () -> Unit) {

    val materialColor = when (scrap.material) {
        "Silk" -> SilkColor
        "Cotton" -> CottonColor
        "Wool" -> WoolColor
        "Linen" -> LinenColor
        "Denim" -> DenimColor
        "Polyester" -> PolyesterColor
        "Rayon" -> RayonColor
        "Chiffon" -> ChiffonColor
        "Georgette" -> GeorgetteColor
        "Velvet" -> VelvetColor
        "Satin" -> SatinColor
        "Khadi" -> KhadiColor
        "Jute" -> JuteColor
        "Nylon" -> NylonColor
        "Blended" -> BlendedColor
        else -> CottonColor
    }

    Card(
        modifier = Modifier
            .padding(8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box {
                if (scrap.photoUrl.isNotEmpty()) {
                    AsyncImage(
                        model = scrap.photoUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .height(140.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .height(140.dp)
                            .fillMaxWidth()
                            .background(materialColor.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = scrap.material.take(1),
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = materialColor
                        )
                    }
                }
                
                Surface(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopEnd),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = "${scrap.sizeMetres} m",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = scrap.material,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = Color(0xFF2D3436)
                )
                if (scrap.color.isNotBlank()) {
                    Text(
                        text = scrap.color,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Text(
                    text = "₹${scrap.price}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = materialColor
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.1f", scrap.distance)} km away",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }
}
