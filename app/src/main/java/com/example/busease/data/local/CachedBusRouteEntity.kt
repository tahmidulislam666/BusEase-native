package com.example.busease.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Room entity representing a locally cached bus route with full stop sequence and metadata
 * for offline persistence and instant offline search.
 */
@Entity(tableName = "cached_bus_routes")
data class CachedBusRouteEntity(
    @PrimaryKey
    val routeId: String,
    val englishName: String,
    val banglaName: String,
    val serviceType: String,
    val image: String,
    val time: String,
    val startStop: String,
    val endStop: String,
    val stopsJson: String, // JSON array of stops
    val stopsCount: Int,
    val cachedAt: Long = System.currentTimeMillis()
)

@Dao
interface CachedBusRouteDao {
    @Query("SELECT * FROM cached_bus_routes ORDER BY englishName ASC")
    fun getAllCachedRoutesFlow(): Flow<List<CachedBusRouteEntity>>

    @Query("SELECT * FROM cached_bus_routes ORDER BY englishName ASC")
    suspend fun getAllCachedRoutes(): List<CachedBusRouteEntity>

    @Query("SELECT * FROM cached_bus_routes WHERE routeId = :routeId LIMIT 1")
    suspend fun getRouteById(routeId: String): CachedBusRouteEntity?

    @Query("SELECT * FROM cached_bus_routes WHERE englishName = :name OR banglaName = :name LIMIT 1")
    suspend fun getRouteByName(name: String): CachedBusRouteEntity?

    @Query("SELECT * FROM cached_bus_routes WHERE englishName LIKE '%' || :query || '%' OR banglaName LIKE '%' || :query || '%' OR stopsJson LIKE '%' || :query || '%'")
    suspend fun searchCachedRoutes(query: String): List<CachedBusRouteEntity>

    @Query("SELECT COUNT(*) FROM cached_bus_routes")
    suspend fun getCachedRoutesCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(routes: List<CachedBusRouteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(route: CachedBusRouteEntity)

    @Query("DELETE FROM cached_bus_routes")
    suspend fun clearAll()
}
