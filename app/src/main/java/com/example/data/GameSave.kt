package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_save")
data class GameSave(
    @PrimaryKey val id: Int = 1,
    val coins: Int = 0,
    val highScore: Int = 0,
    val selectedCarId: Int = 0,
    // Upgrades
    val speedLevel: Int = 1,      // Speed scaling
    val accelerationLevel: Int = 1, // Acceleration boost
    val shieldLevel: Int = 1,     // Additional health points or shields (durability)
    val handlingLevel: Int = 1,   // Snappier lane shifting physics
    val coinValueLevel: Int = 1,  // Bronze to Silver to Gold coin chance & value
    val unlockedCarsBits: Int = 1, // Bitmask for unlocked cars. Car 0 is always unlocked (1 in binary)
    val language: String = "en"   // "en" (English) or "it" (Italian)
) {
    // Helper to check if a car is unlocked
    fun isCarUnlocked(carId: Int): Boolean {
        return (unlockedCarsBits and (1 shl carId)) != 0
    }

    // Helper to unlock a car
    fun unlockCar(carId: Int): GameSave {
        return this.copy(unlockedCarsBits = unlockedCarsBits or (1 shl carId))
    }
}
