package com.wasama.hustlehub.data.local

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

class Converters {
    @TypeConverter fun fromStatus(s: ProjectStatus): String = s.name
    @TypeConverter fun toStatus(s: String): ProjectStatus = try { ProjectStatus.valueOf(s) } catch(e: Exception) { ProjectStatus.PROPOSED }
}

val MIGRATION_2_3 = object : Migration(2,3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        try { db.execSQL("ALTER TABLE invoices ADD COLUMN issueDate INTEGER NOT NULL DEFAULT 0") } catch(_:Exception){}
        try { db.execSQL("ALTER TABLE invoices ADD COLUMN dueDate INTEGER NOT NULL DEFAULT 0") } catch(_:Exception){}
    }
}
val MIGRATION_1_2_SAFE = object : Migration(1,2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        try { db.execSQL("ALTER TABLE clients ADD COLUMN phone TEXT") } catch(_:Exception){}
        try { db.execSQL("ALTER TABLE projects ADD COLUMN description TEXT NOT NULL DEFAULT ''") } catch(_:Exception){}
        try { db.execSQL("ALTER TABLE projects ADD COLUMN tasksJson TEXT NOT NULL DEFAULT '[]'") } catch(_:Exception){}
    }
}

@Database(entities=[UserEntity::class, ClientEntity::class, ProjectEntity::class, TaskEntity::class, TimeEntryEntity::class, InvoiceEntity::class, ExchangeRateCache::class], version=3, exportSchema=false)
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
                .addMigrations(MIGRATION_1_2_SAFE, MIGRATION_2_3)
                .fallbackToDestructiveMigration()
                .build().also { INSTANCE = it }
        }
        fun clearAndGet(context: Context): HustleHubDatabase {
            context.deleteDatabase("hustlehub.db")
            INSTANCE = null
            return get(context)
        }
    }
}

@Dao interface ClientDao {
    @Query("SELECT * FROM clients WHERE userId = :userId ORDER BY createdAt DESC") fun getClients(userId: String): Flow<List<ClientEntity>>
    @Query("SELECT * FROM clients ORDER BY createdAt DESC") fun getAllClients(): Flow<List<ClientEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(client: ClientEntity)
    @Delete suspend fun delete(client: ClientEntity)
    @Query("SELECT * FROM clients WHERE clientId = :id LIMIT 1") suspend fun getById(id: String): ClientEntity?
}
@Dao interface ProjectDao {
    @Query("SELECT * FROM projects WHERE userId = :userId ORDER BY deadlineTimestamp ASC") fun getProjects(userId: String): Flow<List<ProjectEntity>>
    @Query("SELECT * FROM projects WHERE projectId = :id LIMIT 1") suspend fun getById(id: String): ProjectEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(project: ProjectEntity)
    @Query("UPDATE projects SET status = :status WHERE projectId = :id") suspend fun updateStatus(id: String, status: ProjectStatus)
    @Query("UPDATE projects SET tasksJson = :json WHERE projectId = :id") suspend fun updateTasks(id: String, json: String)
    @Query("UPDATE projects SET description = :desc WHERE projectId = :id") suspend fun updateDescription(id: String, desc: String)
    @Query("UPDATE projects SET deadlineTimestamp = :ts WHERE projectId = :id") suspend fun updateDeadline(id: String, ts: Long)
    @Query("UPDATE projects SET totalTrackedMinutes = totalTrackedMinutes + :mins WHERE projectId = :id") suspend fun addMinutes(id: String, mins: Int)
    @Delete suspend fun delete(project: ProjectEntity)
    @Query("DELETE FROM projects") suspend fun clearAll()
}
@Dao interface InvoiceDao {
    @Query("SELECT * FROM invoices WHERE userId = :userId ORDER BY dueDate ASC") fun getInvoices(userId: String): Flow<List<InvoiceEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(invoice: InvoiceEntity)
    @Query("UPDATE invoices SET isPaid = 1 WHERE invoiceId = :id") suspend fun markPaid(id: String)
    @Query("DELETE FROM invoices") suspend fun clearAll()
}
@Dao interface TaskDao {
    @Query("SELECT * FROM tasks WHERE projectId = :projectId ORDER BY createdAt ASC") fun getTasks(projectId: String): Flow<List<TaskEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(task: TaskEntity)
    @Query("UPDATE tasks SET isCompleted = :completed WHERE taskId = :id") suspend fun setCompleted(id: String, completed: Boolean)
    @Delete suspend fun delete(task: TaskEntity)
}
@Dao interface TimeDao {
    @Query("SELECT * FROM time_entries WHERE projectId = :projectId ORDER BY startTime DESC") fun getForProject(projectId: String): Flow<List<TimeEntryEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(entry: TimeEntryEntity)
    @Query("SELECT SUM(durationMinutes) FROM time_entries WHERE projectId = :projectId") suspend fun totalMinutes(projectId: String): Int?
}
@Dao interface RateDao {
    @Query("SELECT * FROM exchange_rate_cache WHERE base = :base LIMIT 1") suspend fun getCache(base: String): ExchangeRateCache?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun save(cache: ExchangeRateCache)
}
