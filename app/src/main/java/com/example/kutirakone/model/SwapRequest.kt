package com.example.kutirakone.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "swap_requests")
data class SwapRequest(
    @PrimaryKey val id: String,
    @Embedded(prefix = "scrap_") val scrap: FabricScrap,
    val status: String = "Pending",
    val timestamp: Long = System.currentTimeMillis()
)
