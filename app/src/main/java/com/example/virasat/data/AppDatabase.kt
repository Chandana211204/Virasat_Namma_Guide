package com.example.virasat.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CheckInEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun checkInDao(): CheckInDao

    companion object {
        fun getDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "virasat_db"
            ).build()
        }
    }
}
