package com.example.busease.data.repository

import android.content.Context
import com.example.busease.data.local.AppDatabase
import com.example.busease.data.local.FavoriteBusEntity
import com.example.busease.data.model.BusRoute
import com.example.busease.data.model.FareItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import androidx.room.Room

class BusRepository private constructor(private val context: Context) {

    private val db = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "busease_local.db"
    )
        .fallbackToDestructiveMigration()
        .build()

    private val favoriteDao = db.favoriteBusDao()

    @Volatile
    private var cachedBuses: List<BusRoute>? = null

    @Volatile
    private var cachedStops: List<String>? = null

    @Volatile
    private var cachedFareStops: List<String>? = null

    @Volatile
    private var cachedFares: List<FareItem>? = null

    suspend fun getAllBuses(): List<BusRoute> = withContext(Dispatchers.IO) {
        cachedBuses?.let { return@withContext it }
        val buses = mutableListOf<BusRoute>()
        try {
            val jsonString = context.assets.open("dhaka-city-local-bus.json").bufferedReader().use { it.readText() }
            val rootObj = JSONObject(jsonString)
            val dataArray = rootObj.optJSONArray("data") ?: JSONArray()
            for (i in 0 until dataArray.length()) {
                val item = dataArray.getJSONObject(i)
                val english = item.optString("english", "")
                val bangla = item.optString("bangle", "")
                val image = item.optString("image", "")
                val serviceType = item.optString("service_type", "")
                val time = item.optString("time", "")
                val routesJson = item.optJSONArray("routes") ?: JSONArray()
                val routesList = mutableListOf<String>()
                for (j in 0 until routesJson.length()) {
                    val stop = routesJson.optString(j, "").trim()
                    if (stop.isNotEmpty()) {
                        routesList.add(stop)
                    }
                }
                if (english.isNotEmpty()) {
                    val start = routesList.firstOrNull() ?: ""
                    val end = routesList.lastOrNull() ?: ""
                    val uniqueId = "${english}_${start}_${end}_$i"
                    buses.add(
                        BusRoute(
                            id = uniqueId,
                            englishName = english,
                            banglaName = bangla,
                            image = image,
                            routes = routesList,
                            serviceType = serviceType,
                            time = time
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        cachedBuses = buses
        buses
    }

    suspend fun getAllStops(): List<String> = withContext(Dispatchers.IO) {
        cachedStops?.let { return@withContext it }
        val buses = getAllBuses()
        val stopSet = sortedSetOf<String>(String.CASE_INSENSITIVE_ORDER)
        for (bus in buses) {
            for (stop in bus.routes) {
                if (stop.isNotBlank()) {
                    stopSet.add(stop)
                }
            }
        }
        val list = stopSet.toList()
        cachedStops = list
        list
    }

    suspend fun searchBusesBetween(start: String, end: String): List<BusRoute> = withContext(Dispatchers.IO) {
        val allBuses = getAllBuses()
        val s = start.trim().lowercase()
        val e = end.trim().lowercase()

        if (s.isEmpty() && e.isEmpty()) return@withContext allBuses

        allBuses.filter { bus ->
            val stopsLower = bus.routes.map { it.lowercase() }
            val startIndex = if (s.isNotEmpty()) stopsLower.indexOfFirst { it.contains(s) } else 0
            val endIndex = if (e.isNotEmpty()) stopsLower.indexOfFirst { it.contains(e) } else stopsLower.lastIndex

            if (s.isNotEmpty() && e.isNotEmpty()) {
                startIndex != -1 && endIndex != -1
            } else if (s.isNotEmpty()) {
                startIndex != -1
            } else {
                endIndex != -1
            }
        }
    }

    suspend fun searchBusesByQuery(query: String): List<BusRoute> = withContext(Dispatchers.IO) {
        val allBuses = getAllBuses()
        val q = query.trim().lowercase()
        if (q.isEmpty()) return@withContext allBuses

        allBuses.filter { bus ->
            bus.englishName.lowercase().contains(q) ||
                    bus.banglaName.lowercase().contains(q) ||
                    bus.serviceType.lowercase().contains(q) ||
                    bus.routes.any { it.lowercase().contains(q) }
        }
    }

    suspend fun getBusById(id: String): BusRoute? = withContext(Dispatchers.IO) {
        getAllBuses().firstOrNull { it.routeId == id || it.id == id }
    }

    suspend fun getBusByName(name: String): BusRoute? = withContext(Dispatchers.IO) {
        getAllBuses().firstOrNull { it.routeId == name || it.id == name || it.englishName.equals(name, ignoreCase = true) }
    }

    suspend fun getFareStops(): List<String> = withContext(Dispatchers.IO) {
        cachedFareStops?.let { return@withContext it }
        val stops = sortedSetOf<String>(String.CASE_INSENSITIVE_ORDER)
        val fares = loadFares()
        for (fare in fares) {
            if (fare.from.isNotBlank()) stops.add(fare.from)
            if (fare.to.isNotBlank()) stops.add(fare.to)
        }
        val list = stops.toList()
        cachedFareStops = list
        list
    }

    private suspend fun loadFares(): List<FareItem> = withContext(Dispatchers.IO) {
        cachedFares?.let { return@withContext it }
        val list = mutableListOf<FareItem>()
        try {
            val jsonString = context.assets.open("kotovara_full_data.json").bufferedReader().use { it.readText() }
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    FareItem(
                        from = obj.optString("From", ""),
                        to = obj.optString("To", ""),
                        distance = obj.optString("Distance", ""),
                        fare = obj.optString("Fare", ""),
                        route = obj.optString("Route", "")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        cachedFares = list
        list
    }

    suspend fun searchFares(fromStop: String, toStop: String): List<FareItem> = withContext(Dispatchers.IO) {
        val fares = loadFares()
        val fromClean = fromStop.trim().lowercase()
        val toClean = toStop.trim().lowercase()

        if (fromClean.isEmpty() && toClean.isEmpty()) return@withContext emptyList()

        fares.filter { item ->
            val matchFrom = fromClean.isEmpty() || item.from.lowercase().contains(fromClean)
            val matchTo = toClean.isEmpty() || item.to.lowercase().contains(toClean)
            matchFrom && matchTo
        }
    }

    fun getAllFavorites(): Flow<List<FavoriteBusEntity>> = favoriteDao.getAllFavorites()

    fun isFavorite(routeId: String): Flow<Boolean> = favoriteDao.isFavorite(routeId)

    suspend fun toggleFavorite(bus: BusRoute) = withContext(Dispatchers.IO) {
        val entity = FavoriteBusEntity(
            routeId = bus.routeId,
            englishName = bus.englishName,
            banglaName = bus.banglaName,
            serviceType = bus.serviceType,
            image = bus.image,
            startStop = bus.startStop,
            endStop = bus.endStop,
            stopsCsv = bus.routes.joinToString(",")
        )
        favoriteDao.insertFavorite(entity)
    }

    suspend fun removeFavorite(routeId: String) = withContext(Dispatchers.IO) {
        favoriteDao.deleteFavorite(routeId)
    }

    companion object {
        @Volatile
        private var instance: BusRepository? = null

        fun getInstance(context: Context): BusRepository {
            return instance ?: synchronized(this) {
                instance ?: BusRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
