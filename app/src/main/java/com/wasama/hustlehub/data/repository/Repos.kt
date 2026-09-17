package com.wasama.hustlehub.data.repository

import com.wasama.hustlehub.data.local.*
import com.wasama.hustlehub.data.remote.*
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow

class ClientRepository(private val dao: ClientDao) {
    fun getClients(uid: String): Flow<List<ClientEntity>> = dao.getClients(uid)
    suspend fun add(client: ClientEntity) = dao.insert(client)
    suspend fun remove(client: ClientEntity) = dao.delete(client)
    fun validate(name: String, email: String): Boolean = name.length >= 2 && email.contains("@")
}

class ProjectRepository(private val dao: ProjectDao) {
    fun getProjects(uid: String) = dao.getProjects(uid)
    suspend fun add(p: ProjectEntity) = dao.insert(p)
    suspend fun move(projectId: String, newStatus: ProjectStatus) = dao.updateStatus(projectId, newStatus)
}

class ExchangeRateRepository(private val api: ExchangeRateApi, private val cacheDao: RateDao) {
    private val gson = Gson()
    suspend fun getZarRate(): Double {
        return try {
            val res = api.getUsdRates()
            cacheDao.save(ExchangeRateCache(base="USD", ratesJson=gson.toJson(res.rates)))
            res.rates["ZAR"] ?: 18.5
        } catch (e: Exception) {
            val cached = cacheDao.getCache("USD")
            if (cached != null) {
                val map: Map<String, Double> = gson.fromJson(cached.ratesJson, object: com.google.gson.reflect.TypeToken<Map<String, Double>>(){}.type)
                map["ZAR"] ?: 18.5
            } else 18.5
        }
    }
}