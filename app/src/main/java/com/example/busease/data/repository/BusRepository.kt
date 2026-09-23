package com.example.busease.data.repository

import android.content.Context
import androidx.room.Room
import com.example.busease.data.local.AppDatabase
import com.example.busease.data.local.CachedBusRouteEntity
import com.example.busease.data.local.FavoriteBusEntity
import com.example.busease.data.model.BusRoute
import com.example.busease.data.model.FareItem
import com.example.busease.data.util.DhakaTransitCoordinates
import com.example.busease.data.util.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class BusRepository private constructor(private val context: Context) {

    private val db = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "busease_local.db"
    )
        .fallbackToDestructiveMigration()
        .build()

    private val favoriteDao = db.favoriteBusDao()
    private val cachedRouteDao = db.cachedBusRouteDao()

    @Volatile
    private var cachedBuses: List<BusRoute>? = null

    @Volatile
    private var cachedStops: List<String>? = null

    @Volatile
    private var cachedFareStops: List<String>? = null

    @Volatile
    private var cachedFares: List<FareItem>? = null

    /**
     * Returns all buses from the local Room database cache first.
     * If the Room database cache is empty, it parses and populates the database
     * from the bundled dataset so it is permanently cached offline.
     */
    suspend fun getAllBuses(): List<BusRoute> = withContext(Dispatchers.IO) {
        cachedBuses?.let { return@withContext it }

        // 1. Check Room database cache first
        try {
            val roomEntities = cachedRouteDao.getAllCachedRoutes()
            if (roomEntities.isNotEmpty()) {
                val mapped = roomEntities.map { entity ->
                    BusRoute(
                        id = entity.routeId,
                        englishName = entity.englishName,
                        banglaName = entity.banglaName,
                        image = entity.image,
                        routes = parseJsonStops(entity.stopsJson),
                        serviceType = entity.serviceType,
                        time = entity.time
                    )
                }
                cachedBuses = mapped
                return@withContext mapped
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Room cache was empty: load initial dataset, cache into Room database, and return
        val buses = mutableListOf<BusRoute>()
        val entitiesToCache = mutableListOf<CachedBusRouteEntity>()
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
                    val bus = BusRoute(
                        id = uniqueId,
                        englishName = english,
                        banglaName = bangla,
                        image = image,
                        routes = routesList,
                        serviceType = serviceType,
                        time = time
                    )
                    buses.add(bus)

                    // Prepare Room Entity
                    val stopsArray = JSONArray(routesList)
                    entitiesToCache.add(
                        CachedBusRouteEntity(
                            routeId = uniqueId,
                            englishName = english,
                            banglaName = bangla,
                            serviceType = serviceType,
                            image = image,
                            time = time,
                            startStop = start,
                            endStop = end,
                            stopsJson = stopsArray.toString(),
                            stopsCount = routesList.size,
                            cachedAt = System.currentTimeMillis()
                        )
                    )
                }
            }

            // Persist full route catalog into Room DB for 100% offline access
            if (entitiesToCache.isNotEmpty()) {
                cachedRouteDao.insertAll(entitiesToCache)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        cachedBuses = buses
        buses
    }

    private fun parseJsonStops(stopsJson: String): List<String> {
        val list = mutableListOf<String>()
        try {
            val arr = JSONArray(stopsJson)
            for (i in 0 until arr.length()) {
                val s = arr.optString(i, "").trim()
                if (s.isNotEmpty()) list.add(s)
            }
        } catch (e: Exception) {
            // Fallback to comma separated
            return stopsJson.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }
        return list
    }

    /**
     * Re-caches/syncs routes into Room Database explicitly.
     */
    suspend fun refreshLocalDatabaseCache(): Int = withContext(Dispatchers.IO) {
        cachedBuses = null
        cachedRouteDao.clearAll()
        val reloaded = getAllBuses()
        reloaded.size
    }

    /**
     * Returns total number of cached routes currently in Room Database.
     */
    suspend fun getCachedRoutesCount(): Int = withContext(Dispatchers.IO) {
        cachedRouteDao.getCachedRoutesCount()
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

    /**
     * Finds bus routes that have one or more stops within [radiusMeters] (default 1000m / 1km)
     * of the specified user latitude and longitude.
     * Returns matching bus routes paired with their nearest stop name and distance in meters.
     */
    suspend fun searchBusesNearLocation(
        userLat: Double,
        userLon: Double,
        radiusMeters: Double = 1000.0
    ): List<NearbyBusMatch> = withContext(Dispatchers.IO) {
        val allBuses = getAllBuses()
        val nearbyStopsWithDist = DhakaTransitCoordinates.getStopsWithinRadius(userLat, userLon, radiusMeters)
        if (nearbyStopsWithDist.isEmpty()) return@withContext emptyList()

        // Create quick lookup for stop name -> distance in meters
        val stopDistanceMap = mutableMapOf<String, Double>()
        for ((stopName, dist) in nearbyStopsWithDist) {
            stopDistanceMap[stopName.lowercase().trim()] = dist
        }

        val results = mutableListOf<NearbyBusMatch>()
        for (bus in allBuses) {
            var minDistance = Double.MAX_VALUE
            var closestStopName = ""

            for (busStop in bus.routes) {
                val clean = busStop.lowercase().trim()
                // Direct lookup or fuzzy match against known nearby stops
                var matchedDist: Double? = stopDistanceMap[clean]
                if (matchedDist == null) {
                    for ((knownStop, dist) in stopDistanceMap) {
                        if (clean.contains(knownStop) || knownStop.contains(clean)) {
                            matchedDist = dist
                            break
                        }
                    }
                }

                if (matchedDist != null && matchedDist < minDistance) {
                    minDistance = matchedDist
                    closestStopName = busStop
                }
            }

            if (minDistance <= radiusMeters && closestStopName.isNotEmpty()) {
                results.add(
                    NearbyBusMatch(
                        bus = bus,
                        nearestStop = closestStopName,
                        distanceMeters = minDistance
                    )
                )
            }
        }

        // Sort by distance to nearest stop (closest first)
        results.sortedBy { it.distanceMeters }
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

    /**
     * Estimates bus fare and distance between two stops on a route.
     * Uses official BRTA fare database if direct or partial match exists,
     * otherwise computes estimated distance using transit coordinates and BRTA government rate
     * (~2.45 BDT/km with minimum fare of 10 BDT).
     */
    suspend fun estimateRouteFare(
        fromStop: String,
        toStop: String,
        routeStops: List<String>
    ): RouteFareEstimate = withContext(Dispatchers.IO) {
        val cleanFrom = fromStop.trim()
        val cleanTo = toStop.trim()
        if (cleanFrom.isEmpty() || cleanTo.isEmpty()) {
            return@withContext RouteFareEstimate(
                fareTaka = 0,
                distanceKm = 0.0,
                stopsTraveled = 0,
                isExactBrtaMatch = false,
                source = "none"
            )
        }

        // 1. Check exact/partial match in kotovara_full_data.json
        val matches = searchFares(cleanFrom, cleanTo)
        val exactMatch = matches.firstOrNull {
            it.from.equals(cleanFrom, ignoreCase = true) && it.to.equals(cleanTo, ignoreCase = true)
        } ?: matches.firstOrNull {
            it.to.equals(cleanFrom, ignoreCase = true) && it.from.equals(cleanTo, ignoreCase = true)
        } ?: matches.firstOrNull()

        // Calculate stops count along routeStops
        val stopsLower = routeStops.map { it.trim().lowercase() }
        val idx1 = stopsLower.indexOf(cleanFrom.lowercase())
        val idx2 = stopsLower.indexOf(cleanTo.lowercase())
        val stopsTraveled = if (idx1 != -1 && idx2 != -1) Math.abs(idx2 - idx1) else 1

        if (exactMatch != null) {
            val parsedFare = exactMatch.fare.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 10
            val parsedDist = exactMatch.distance.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: (stopsTraveled * 1.4)
            return@withContext RouteFareEstimate(
                fareTaka = parsedFare,
                distanceKm = parsedDist,
                stopsTraveled = stopsTraveled,
                isExactBrtaMatch = true,
                source = "BRTA Chart (${exactMatch.from} - ${exactMatch.to})"
            )
        }

        // 2. Coordinate-based or inter-stop distance estimation
        var distKm = 0.0
        val defaultCenter = LatLng(23.7570, 90.3888)
        if (idx1 != -1 && idx2 != -1 && idx1 != idx2) {
            val startIdx = Math.min(idx1, idx2)
            val endIdx = Math.max(idx1, idx2)
            var accumMeters = 0.0
            for (i in startIdx until endIdx) {
                val s1 = routeStops[i]
                val s2 = routeStops[i + 1]
                val c1 = DhakaTransitCoordinates.getStopCoordinate(s1) ?: defaultCenter
                val c2 = DhakaTransitCoordinates.getStopCoordinate(s2) ?: defaultCenter
                val legMeters = DhakaTransitCoordinates.calculateDistanceMeters(
                    c1.latitude, c1.longitude,
                    c2.latitude, c2.longitude
                )
                accumMeters += if (legMeters < 100.0) 1300.0 else legMeters
            }
            distKm = accumMeters / 1000.0
        } else {
            val c1 = DhakaTransitCoordinates.getStopCoordinate(cleanFrom) ?: defaultCenter
            val c2 = DhakaTransitCoordinates.getStopCoordinate(cleanTo) ?: LatLng(23.7340, 90.4125)
            val directMeters = DhakaTransitCoordinates.calculateDistanceMeters(
                c1.latitude, c1.longitude,
                c2.latitude, c2.longitude
            )
            distKm = if (directMeters < 200.0) (stopsTraveled * 1.3) else (directMeters / 1000.0)
        }

        if (distKm < 0.5) {
            distKm = Math.max(1.0, stopsTraveled * 1.3)
        }

        // Dhaka City bus government rate: ~৳2.45 to ৳2.50 per km, minimum fare ৳10
        val baseFare = (distKm * 2.45).toInt()
        val finalFare = Math.max(10, Math.ceil(baseFare.toDouble() / 5.0).toInt() * 5) // Rounded to standard 5 Taka denominations

        RouteFareEstimate(
            fareTaka = finalFare,
            distanceKm = distKm,
            stopsTraveled = stopsTraveled,
            isExactBrtaMatch = false,
            source = "BRTA Standard Rate (~৳2.45/km, Min ৳10)"
        )
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

/**
 * Result model representing a bus route passing through a stop within radius of the user.
 */
data class NearbyBusMatch(
    val bus: BusRoute,
    val nearestStop: String,
    val distanceMeters: Double
)

/**
 * Result model for estimated bus fare between two stops along a route.
 */
data class RouteFareEstimate(
    val fareTaka: Int,
    val distanceKm: Double,
    val stopsTraveled: Int,
    val isExactBrtaMatch: Boolean,
    val source: String
)
