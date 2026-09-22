package com.wasama.hustlehub.data.repository

import com.google.gson.Gson
import com.wasama.hustlehub.data.local.*
import kotlinx.coroutines.flow.Flow

class ProjectRepositoryV2(private val dao: ProjectDao) {
    private val gson = Gson()

    fun getProjects(uid: String): Flow<List<ProjectEntity>> = dao.getProjects(uid)

    suspend fun addProject(project: ProjectEntity, taskTitles: List<String>) {
        val limitedTasks = taskTitles.take(10).map { TaskItem(title = it) }
        val json = gson.toJson(limitedTasks)
        dao.insert(project.copy(tasksJson = json))
    }

    suspend fun updateTasks(projectId: String, tasks: List<TaskItem>) {
        val limited = tasks.take(10)
        dao.updateTasks(projectId, gson.toJson(limited))
    }

    suspend fun updateDescription(projectId: String, description: String) {
        dao.updateDescription(projectId, description)
    }

    suspend fun moveStatus(projectId: String, newStatus: ProjectStatus) {
        dao.updateStatus(projectId, newStatus)
    }

    fun validateProject(title: String, description: String, tasks: List<String>): Boolean {
        return title.length >= 2 && description.length <= 500 && tasks.size <= 10
    }
}
