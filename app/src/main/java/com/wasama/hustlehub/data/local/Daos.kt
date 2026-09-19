package com.wasama.hustlehub.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao interface ClientDao {
    @Query("SELECT * FROM clients WHERE userId = :userId ORDER BY createdAt DESC")
    fun getClients(userId: String): Flow<List<ClientEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(client: ClientEntity): Long
    @Delete suspend fun delete(client: ClientEntity): Int
    @Query("SELECT * FROM clients WHERE clientId = :id LIMIT 1") suspend fun getById(id: String): ClientEntity?
}

@Dao interface ProjectDao {
    @Query("SELECT * FROM projects WHERE userId = :userId ORDER BY deadlineTimestamp ASC")
    fun getProjects(userId: String): Flow<List<ProjectEntity>>
    @Query("SELECT * FROM projects WHERE status = :status AND userId = :userId")
    fun getByStatus(status: ProjectStatus, userId: String): Flow<List<ProjectEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(project: ProjectEntity): Long
    @Query("UPDATE projects SET status = :status WHERE projectId = :id") suspend fun updateStatus(id: String, status: ProjectStatus): Int
    @Query("UPDATE projects SET totalTrackedMinutes = totalTrackedMinutes + :mins WHERE projectId = :id") suspend fun addMinutes(id: String, mins: Int): Int
    @Query("SELECT * FROM projects WHERE projectId = :id LIMIT 1") suspend fun getById(id: String): ProjectEntity?
    @Delete suspend fun delete(project: ProjectEntity): Int
}

@Dao interface TaskDao {
    @Query("SELECT * FROM tasks WHERE projectId = :projectId ORDER BY createdAt ASC") fun getTasks(projectId: String): Flow<List<TaskEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(task: TaskEntity): Long
    @Query("UPDATE tasks SET isCompleted = :completed WHERE taskId = :id") suspend fun setCompleted(id: String, completed: Boolean): Int
    @Delete suspend fun delete(task: TaskEntity): Int
}

@Dao interface TimeDao {
    @Query("SELECT * FROM time_entries WHERE projectId = :projectId ORDER BY startTime DESC") fun getForProject(projectId: String): Flow<List<TimeEntryEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(entry: TimeEntryEntity): Long
    @Query("SELECT SUM(durationMinutes) FROM time_entries WHERE projectId = :projectId") suspend fun totalMinutes(projectId: String): Int?
}

@Dao interface InvoiceDao {
    @Query("SELECT * FROM invoices WHERE userId = :userId ORDER BY createdAt DESC") fun getInvoices(userId: String): Flow<List<InvoiceEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(invoice: InvoiceEntity): Long
    @Query("UPDATE invoices SET isPaid = 1 WHERE invoiceId = :id") suspend fun markPaid(id: String): Int
}

@Dao interface RateDao {
    @Query("SELECT * FROM exchange_rate_cache WHERE base = :base LIMIT 1") suspend fun getCache(base: String): ExchangeRateCache?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun save(cache: ExchangeRateCache)
}
