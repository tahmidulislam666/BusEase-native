package com.example.busease.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "favorite_buses")
data class FavoriteBusEntity(
    @PrimaryKey
    val routeId: String,
    val englishName: String,
    val banglaName: String,
    val serviceType: String,
    val image: String,
    val startStop: String,
    val endStop: String,
    val stopsCsv: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Dao
interface FavoriteBusDao {
    @Query("SELECT * FROM favorite_buses ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteBusEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_buses WHERE routeId = :routeId)")
    fun isFavorite(routeId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(bus: FavoriteBusEntity)

    @Query("DELETE FROM favorite_buses WHERE routeId = :routeId")
    suspend fun deleteFavorite(routeId: String)
}

@Database(entities = [FavoriteBusEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteBusDao(): FavoriteBusDao
}
