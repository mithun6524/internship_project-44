package com.example.kutirakone.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "fabric_scraps")
data class FabricScrap(
    @PrimaryKey val id: String = "",
    val ownerUid: String = "",
    val material: String = "",
    val color: String = "",
    val sizeMetres: Double = 0.0,
    val price: Double = 0.0,
    val photoUrl: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val address: String = "",
    val phone: String = "",
    val timestamp: Long = 0L
) {
    @Ignore var distance: Double = 0.0
}
