package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GameRepository(private val dao: GameSaveDao) {
    val gameSaveFlow: Flow<GameSave> = dao.getGameSaveFlow().map {
        it ?: GameSave() // Return default empty progression if null (first run)
    }

    suspend fun getGameSave(): GameSave {
        return dao.getGameSave() ?: GameSave()
    }

    suspend fun saveProgress(gameSave: GameSave) {
        dao.insertOrUpdate(gameSave)
    }

    suspend fun addCoins(amount: Int) {
        val current = getGameSave()
        saveProgress(current.copy(coins = current.coins + amount))
    }

    suspend fun updateHighScore(newScore: Int) {
        val current = getGameSave()
        if (newScore > current.highScore) {
            saveProgress(current.copy(highScore = newScore))
        }
    }

    suspend fun selectCar(carId: Int) {
        val current = getGameSave()
        if (current.isCarUnlocked(carId)) {
            saveProgress(current.copy(selectedCarId = carId))
        }
    }
}
