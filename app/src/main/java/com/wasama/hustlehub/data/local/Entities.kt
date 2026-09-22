package com.wasama.hustlehub.data.local

import androidx.room.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID

enum class ProjectStatus { PROPOSED, IN_PROGRESS, DONE, INVOICED, PAID }

@Entity(tableName = "clients")
data class ClientEntity(
    @PrimaryKey val clientId: String = UUID.randomUUID().toString(),
    val userId: String = "current_user",
    val name: String,
    val email: String = "",
    val company: String? = null,
    val ratePerHour: Double = 250.0,
    val phone: String? = null,
    val trustScore: Int = 85,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val projectId: String = UUID.randomUUID().toString(),
    val userId: String = "current_user",
    val clientId: String = "",
    val title: String,
    val description: String = "",
    val budgetAmount: Double = 5000.0,
    val budgetCurrency: String = "ZAR",
    val convertedBudgetZAR: Double? = null,
    val deadlineTimestamp: Long = System.currentTimeMillis() + 2*24*60*60*1000L, // NEW: drives overdue
    val status: ProjectStatus = ProjectStatus.PROPOSED,
    val progressPercent: Int = 0,
    val totalTrackedMinutes: Int = 0,
    val tasksJson: String = "[]",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getTasksListSafe(): List<TaskItem> {
        return try {
            if (tasksJson.isBlank() || tasksJson == "null") return emptyList()
            val type = object : TypeToken<List<TaskItem>>() {}.type
            Gson().fromJson<List<TaskItem>>(tasksJson, type) ?: emptyList()
        } catch (e: Exception) { emptyList() }
    }
    fun daysLeft(): Long = (deadlineTimestamp - System.currentTimeMillis()) / (24*60*60*1000L)
    fun isOverdue(): Boolean = deadlineTimestamp < System.currentTimeMillis() && status != ProjectStatus.PAID
}

data class TaskItem(val title: String = "", val isCompleted: Boolean = false)

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey val invoiceId: String = UUID.randomUUID().toString(),
    val projectId: String,
    val clientId: String = "",
    val userId: String = "current_user",
    val amount: Double,
    val currency: String = "ZAR",
    val isPaid: Boolean = false,
    val issueDate: Long = System.currentTimeMillis(), // NEW
    val dueDate: Long = System.currentTimeMillis() + 14*24*60*60*1000L, // NEW: 14 days default - drives overdue
    val pdfFilePath: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun isOverdue(): Boolean = !isPaid && dueDate < System.currentTimeMillis()
}

@Entity(tableName = "users")
data class UserEntity(@PrimaryKey val userId: String, val email: String, val hourlyRate: Double=250.0, val xp:Int=0, val streak:Int=0, val badges:String="", val freezeTokens:Int=1, val createdAt:Long=System.currentTimeMillis())

@Entity(tableName = "time_entries")
data class TimeEntryEntity(@PrimaryKey val timeEntryId: String=UUID.randomUUID().toString(), val projectId:String, val userId:String="current_user", val startTime:Long, val endTime:Long?=null, val durationMinutes:Int=0, val isBillable:Boolean=true)

@Entity(tableName = "exchange_rate_cache")
data class ExchangeRateCache(@PrimaryKey val base:String="USD", val ratesJson:String, val timestamp:Long=System.currentTimeMillis())

@Entity(tableName = "tasks")
data class TaskEntity(@PrimaryKey val taskId:String=UUID.randomUUID().toString(), val projectId:String, val title:String, val isCompleted:Boolean=false, val dueDate:Long?=null, val xpAwarded:Int=0, val createdAt:Long=System.currentTimeMillis())
