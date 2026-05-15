package com.example.virasat.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "checkins")
data class CheckInEntity(
    @PrimaryKey val siteId: String,
    val siteName: String,
    val checkedInAt: Long
)