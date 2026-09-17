package com.wasama.hustlehub.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class ProjectStatus { PROPOSED, IN_PROGRESS, DONE, INVOICED, PAID }

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String,
    val email: String,
    val hourlyRate: Double = 250.0,
    val xp: Int = 0,
    val streak: Int = 0,
    val badges: String = "", // comma separated
    val freezeTokens: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
) {
    val level: Int get() = (xp / 300) + 1
}

@Entity(tableName = "clients")
data class ClientEntity(
    @PrimaryKey val clientId: String = UUID.randomUUID().toString(),
    val userId: String,
    val name: String,
    val email: String,
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
    val userId: String,
    val clientId: String,
    val title: String,
    val description: String? = null,
    val budgetAmount: Double,
    val budgetCurrency: String = "ZAR",
    val convertedBudgetZAR: Double? = null,
    val deadlineTimestamp: Long,
    val status: ProjectStatus = ProjectStatus.PROPOSED,
    val progressPercent: Int = 0,
    val totalTrackedMinutes: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val taskId: String = UUID.randomUUID().toString(),
    val projectId: String,
    val title: String,
    val isCompleted: Boolean = false,
    val dueDate: Long? = null,
    val xpAwarded: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "time_entries")
data class TimeEntryEntity(
    @PrimaryKey val timeEntryId: String = UUID.randomUUID().toString(),
    val projectId: String,
    val userId: String,
    val startTime: Long,
    val endTime: Long? = null,
    val durationMinutes: Int = 0,
    val isBillable: Boolean = true
)

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey val invoiceId: String = UUID.randomUUID().toString(),
    val projectId: String,
    val clientId: String,
    val userId: String,
    val amount: Double,
    val currency: String = "ZAR",
    val isPaid: Boolean = false,
    val pdfFilePath: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "exchange_rate_cache")
data class ExchangeRateCache(
    @PrimaryKey val base: String = "USD",
    val ratesJson: String,
    val timestamp: Long = System.currentTimeMillis()
)