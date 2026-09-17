package com.wasama.hustlehub.data.local

import android.content.Context
import androidx.room.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter fun fromStatus(s: ProjectStatus): String = s.name
    @TypeConverter fun toStatus(s: String): ProjectStatus = ProjectStatus.valueOf(s)
}

@Database(entities = [UserEntity::class, ClientEntity::class, ProjectEntity::class, TaskEntity::class, TimeEntryEntity::class, InvoiceEntity::class, ExchangeRateCache::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class HustleHubDatabase: RoomDatabase() {
    abstract fun clientDao(): ClientDao
    abstract fun projectDao(): ProjectDao
    abstract fun taskDao(): TaskDao
    abstract fun timeDao(): TimeDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun rateDao(): RateDao

    companion object {
        @Volatile private var INSTANCE: HustleHubDatabase? = null
        fun get(context: Context): HustleHubDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(context.applicationContext, HustleHubDatabase::class.java, "hustlehub.db")
                .fallbackToDestructiveMigration().build().also { INSTANCE = it }
        }
    }
}