package com.mat.tracker

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: LocationData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocations(locations: List<LocationData>)

    @Query("SELECT * FROM LocationData")
    suspend fun getAllLocations(): List<LocationData>

    @Query("SELECT EXISTS (SELECT 1 FROM LocationData)")
    suspend fun isNotEmpty(): Boolean

    @Query("DELETE FROM LocationData")
    suspend fun nukeTable()

}