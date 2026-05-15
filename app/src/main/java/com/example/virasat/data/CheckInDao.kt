package com.example.virasat.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CheckInDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun checkIn(entity: CheckInEntity)

    @Query("SELECT * FROM checkins ORDER BY checkedInAt DESC")
    suspend fun getAllCheckIns(): List<CheckInEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM checkins WHERE siteId = :siteId)")
    suspend fun isCheckedIn(siteId: String): Boolean
}
