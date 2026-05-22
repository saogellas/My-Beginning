package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameSaveDao {
    @Query("SELECT * FROM game_save WHERE id = 1 LIMIT 1")
    fun getGameSaveFlow(): Flow<GameSave?>

    @Query("SELECT * FROM game_save WHERE id = 1 LIMIT 1")
    suspend fun getGameSave(): GameSave?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(gameSave: GameSave)
}
