package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.GameRepository
import com.example.data.GameSave
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

enum class GameState {
    MENU,
    SHOP,
    PLAYING,
    GAME_OVER,
    STAGE_CLEAR,
    SETTINGS
}

enum class ObstacleType {
    BLUE_CAR,
    ORANGE_CAR,
    TRUCK,
    ROADBLOCK,
    OIL_SPILL,
    TUMBLEWEED,
    SAND_TRAP,
    ICE_PATCH,
    SNOW_DRIFT,
    SPIKE_STRIP,
    TRAFFIC_CONE
}

enum class LevelTheme(
    val id: Int,
    val displayName: String,
    val description: String,
    val grassColor: androidx.compose.ui.graphics.Color,
    val roadColor: androidx.compose.ui.graphics.Color,
    val curbColor1: androidx.compose.ui.graphics.Color,
    val curbColor2: androidx.compose.ui.graphics.Color,
    val dashColor: androidx.compose.ui.graphics.Color,
    val sceneryTypes: List<String>
) {
    NEON_OVERDRIVE(
        id = 0,
        displayName = "Neon Overdrive",
        description = "Sleek synthwave neon grid streets",
        grassColor = androidx.compose.ui.graphics.Color(0xFF0F0F0F),
        roadColor = androidx.compose.ui.graphics.Color(0xFF1E1E1E),
        curbColor1 = androidx.compose.ui.graphics.Color(0xFFFF0055),
        curbColor2 = androidx.compose.ui.graphics.Color(0xFFD0BCFF),
        dashColor = androidx.compose.ui.graphics.Color(0xFF00E5FF).copy(alpha = 0.5f),
        sceneryTypes = listOf("TREE", "FLOWER")
    ),
    DESERT_HIGHWAY(
        id = 1,
        displayName = "Desert Highway",
        description = "Sun-drenched canyon sands with blowing tumbleweeds",
        grassColor = androidx.compose.ui.graphics.Color(0xFFE9C46A),
        roadColor = androidx.compose.ui.graphics.Color(0xFF4A4E69),
        curbColor1 = androidx.compose.ui.graphics.Color(0xFFF4A261),
        curbColor2 = androidx.compose.ui.graphics.Color(0xFFE76F51),
        dashColor = androidx.compose.ui.graphics.Color(0xFFF4A261).copy(alpha = 0.6f),
        sceneryTypes = listOf("CACTUS", "ROCK")
    ),
    ICY_PASS(
        id = 2,
        displayName = "Icy Mountain Pass",
        description = "Glacial frozen slopes and slippery ice sheets",
        grassColor = androidx.compose.ui.graphics.Color(0xFFE0F7FA),
        roadColor = androidx.compose.ui.graphics.Color(0xFF37474F),
        curbColor1 = androidx.compose.ui.graphics.Color(0xFF00E5FF),
        curbColor2 = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
        dashColor = androidx.compose.ui.graphics.Color(0xFFB2EBF2).copy(alpha = 0.6f),
        sceneryTypes = listOf("TREE", "ROCK")
    ),
    URBAN_CITY(
        id = 3,
        displayName = "Urban Cityscape",
        description = "Dusk-lit downtown concrete jungles with spike strips",
        grassColor = androidx.compose.ui.graphics.Color(0xFF2B2D42),
        roadColor = androidx.compose.ui.graphics.Color(0xFF1B1B1D),
        curbColor1 = androidx.compose.ui.graphics.Color(0xFFEF233C),
        curbColor2 = androidx.compose.ui.graphics.Color(0xFF8D99AE),
        dashColor = androidx.compose.ui.graphics.Color(0xFFF1FAEE).copy(alpha = 0.6f),
        sceneryTypes = listOf("ROCK", "FLOWER")
    );

    companion object {
        fun getById(id: Int): LevelTheme = values().firstOrNull { it.id == id } ?: NEON_OVERDRIVE
    }
}

// Concrete data classes for gameplay elements in Virtual 1000x1000 units
data class Obstacle(
    val id: Long,
    val type: ObstacleType,
    val lane: Int,
    var x: Float,
    var y: Float,
    val width: Float,
    val height: Float,
    val speedY: Float,
    var angle: Float = 0f,
    var active: Boolean = true
)

data class Coin(
    val id: Long,
    val lane: Int,
    var x: Float,
    var y: Float,
    val valAmount: Int,  // 1 for Bronze, 3 for Silver, 5 for Gold
    val isGold: Boolean,
    val isSilver: Boolean,
    var collected: Boolean = false,
    var magneticPullX: Float = 0f,
    var magneticPullY: Float = 0f
)

data class SidelineObject(
    val type: String, // "TREE", "CACTUS", "ROCK", "FLOWER"
    val isLeft: Boolean,
    var x: Float,
    var y: Float,
    val scale: Float,
    val rotation: Float
)

data class RetroParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: androidx.compose.ui.graphics.Color,
    val size: Float,
    var life: Float, // Current frame lifetime
    val maxLife: Float
)

data class SkidMark(
    val lane: Int,
    val x: Float,
    var y: Float,
    var opacity: Float = 0.8f
)

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: GameRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = GameRepository(db.gameSaveDao())
    }

    // Connect user save file reactive Flow
    val gameSaveState: StateFlow<GameSave> = repository.gameSaveFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = GameSave()
        )

    // Current screen layout state
    var currentScreen by mutableStateOf(GameState.MENU)
        private set

    // Tutorial overlay timer
    var tutorialTimerRemaining by mutableStateOf(0f)

    // Guided Tutorial State
    var isGuidedTutorialActive by mutableStateOf(false)
        private set
    var guidedTutorialStep by mutableStateOf(0)
        private set
    var tutorialLeftClicked by mutableStateOf(false)
        private set
    var tutorialRightClicked by mutableStateOf(false)
        private set
    var tutorialCarLane by mutableStateOf(1) // 0, 1, 2
        private set


    // Real-time gameplay variables
    var activeCarIndex by mutableStateOf(0)
    var currentScore by mutableStateOf(0)
    var coinsCollectedThisRun by mutableStateOf(0)
    
    // Level Theme & Progress State (New!)
    var activeTheme by mutableStateOf(LevelTheme.NEON_OVERDRIVE)
    var levelProgressDistance by mutableStateOf(0f)
    var nearMissesCount by mutableStateOf(0)
    var timeTaken by mutableStateOf(0f)
    private val nearMissedObstacles = mutableSetOf<Long>()
    
    // Core physics state for active driver
    var playerX by mutableStateOf(500f) // Virtual center (0 to 1000)
    var playerY by mutableStateOf(820f) // virtual Y offset fixed
    var targetX by mutableStateOf(500f)
    var playerLane by mutableStateOf(1)  // 0, 1, or 2
    var playerAngle by mutableStateOf(0f)
    
    // Game speed parameters
    var baseScrollSpeed by mutableStateOf(450f) // units per second
    var currentScrollSpeed by mutableStateOf(450f)
    var speedMultiplier by mutableStateOf(1.0f)
    
    // Health and state checks
    var maxShields by mutableStateOf(1)
    var currentShields by mutableStateOf(1)
    var isInvulnerable by mutableStateOf(false)
    var invulnerabilityTimer by mutableStateOf(0f)
    var spinOutTimer by mutableStateOf(0f)
    
    // Control slides and screen shake
    var oilSlideSpin by mutableStateOf(0f) // Temporary vehicle yaw shift on oil
    var screenShakeAmount by mutableStateOf(0f)
    
    // Play lists for game loop drawing
    val obstacles = mutableStateListOf<Obstacle>()
    val coins = mutableStateListOf<Coin>()
    val sidelineLeft = mutableStateListOf<SidelineObject>()
    val sidelineRight = mutableStateListOf<SidelineObject>()
    val particles = mutableStateListOf<RetroParticle>()
    val skidMarks = mutableStateListOf<SkidMark>()
    
    // Background dash line scrolling Y offset
    var dashLinesYOffset by mutableStateOf(0f)

    private var spawnTimer = 0f
    private var coinSpawnTimer = 0f
    var gameTimeSec = 0f

    // Reference to game animation loop coroutine job
    private var gameLoopJob: Job? = null

    fun navigateTo(state: GameState) {
        val previousScreen = currentScreen
        currentScreen = state
        if (state == GameState.PLAYING) {
            val showTutorial = (previousScreen == GameState.MENU)
            startNewGameRun(showTutorial = showTutorial)
        } else {
            stopGameLoop()
        }
    }

    fun skipTutorial() {
        tutorialTimerRemaining = 0f
    }

    fun startGuidedTutorial() {
        isGuidedTutorialActive = true
        guidedTutorialStep = 0
        tutorialLeftClicked = false
        tutorialRightClicked = false
        tutorialCarLane = 1
    }

    fun closeGuidedTutorial() {
        isGuidedTutorialActive = false
    }

    fun nextGuidedTutorialStep() {
        if (guidedTutorialStep < 2) {
            guidedTutorialStep++
        } else {
            closeGuidedTutorial()
        }
    }

    fun prevGuidedTutorialStep() {
        if (guidedTutorialStep > 0) {
            guidedTutorialStep--
        }
    }

    fun simulateTutorialSteerLeft() {
        tutorialLeftClicked = true
        if (tutorialCarLane > 0) {
            tutorialCarLane--
        }
    }

    fun simulateTutorialSteerRight() {
        tutorialRightClicked = true
        if (tutorialCarLane < 2) {
            tutorialCarLane++
        }
    }

    // Vehicle unlocks and custom select
    fun selectVehicle(carId: Int) {
        viewModelScope.launch {
            repository.selectCar(carId)
        }
    }

    fun unlockVehicle(carId: Int, cost: Int) {
        viewModelScope.launch {
            val progress = repository.getGameSave()
            if (progress.coins >= cost && !progress.isCarUnlocked(carId)) {
                val updated = progress.copy(coins = progress.coins - cost).unlockCar(carId)
                repository.saveProgress(updated)
                repository.selectCar(carId)
            }
        }
    }

    // Upgrades transaction purchases
    fun purchaseUpgrade(type: String) {
        viewModelScope.launch {
            val progress = repository.getGameSave()
            val cost: Int
            val updatedProgress: GameSave

            when (type) {
                "SPEED" -> {
                    val nextLvl = progress.speedLevel + 1
                    if (nextLvl > 5) return@launch
                    cost = nextLvl * 40
                    if (progress.coins >= cost) {
                        updatedProgress = progress.copy(
                            coins = progress.coins - cost,
                            speedLevel = nextLvl
                        )
                        repository.saveProgress(updatedProgress)
                    }
                }
                "ACCELERATION" -> {
                    val nextLvl = progress.accelerationLevel + 1
                    if (nextLvl > 5) return@launch
                    cost = nextLvl * 45
                    if (progress.coins >= cost) {
                        updatedProgress = progress.copy(
                            coins = progress.coins - cost,
                            accelerationLevel = nextLvl
                        )
                        repository.saveProgress(updatedProgress)
                    }
                }
                "SHIELD" -> {
                    val nextLvl = progress.shieldLevel + 1
                    if (nextLvl > 5) return@launch
                    cost = nextLvl * 50
                    if (progress.coins >= cost) {
                        updatedProgress = progress.copy(
                            coins = progress.coins - cost,
                            shieldLevel = nextLvl
                        )
                        repository.saveProgress(updatedProgress)
                    }
                }
                "HANDLING" -> {
                    val nextLvl = progress.handlingLevel + 1
                    if (nextLvl > 5) return@launch
                    cost = nextLvl * 35
                    if (progress.coins >= cost) {
                        updatedProgress = progress.copy(
                            coins = progress.coins - cost,
                            handlingLevel = nextLvl
                        )
                        repository.saveProgress(updatedProgress)
                    }
                }
                "COIN" -> {
                    val nextLvl = progress.coinValueLevel + 1
                    if (nextLvl > 5) return@launch
                    cost = nextLvl * 45
                    if (progress.coins >= cost) {
                        updatedProgress = progress.copy(
                            coins = progress.coins - cost,
                            coinValueLevel = nextLvl
                        )
                        repository.saveProgress(updatedProgress)
                    }
                }
            }
        }
    }

    // Game Control inputs
    fun moveLaneLeft() {
        if (spinOutTimer > 0f) return
        if (playerLane > 0) {
            playerLane--
            updateTargetLane()
            spawnLaneChangeSkids()
        }
    }

    fun moveLaneRight() {
        if (spinOutTimer > 0f) return
        if (playerLane < 2) {
            playerLane++
            updateTargetLane()
            spawnLaneChangeSkids()
        }
    }

    private fun updateTargetLane() {
        targetX = when (playerLane) {
            0 -> 300f  // Left Lane
            1 -> 500f  // Center Lane
            else -> 700f // Right Lane
        }
    }

    private fun spawnLaneChangeSkids() {
        // Emit skid particles and leave trails on road
        val startMarkX = playerX
        skidMarks.add(SkidMark(playerLane, startMarkX, playerY + 30f))
        
        // Spawn standard rubber-tire particles
        for (i in 0..6) {
            particles.add(
                RetroParticle(
                    x = playerX + Random.nextFloat() * 30f - 15f,
                    y = playerY + 50f,
                    vx = (Random.nextFloat() * 100f - 50f),
                    vy = (Random.nextFloat() * 50f + 50f),
                    color = androidx.compose.ui.graphics.Color(0xFF2B2B2B),
                    size = 6f + Random.nextFloat() * 10f,
                    life = 0f,
                    maxLife = 0.4f
                )
            )
        }
    }

    // Game loop initiation
    fun startNewGameRun(showTutorial: Boolean = false) {
        viewModelScope.launch {
            tutorialTimerRemaining = if (showTutorial) 5.0f else 0f
            val snapshot = repository.getGameSave()
            val carDef = PixelSprites.UnlockedCarsList.getOrNull(snapshot.selectedCarId) ?: PixelSprites.UnlockedCarsList[0]
            
            // Build player dynamic starting traits
            maxShields = 1 + carDef.extraShields + (snapshot.shieldLevel - 1)
            currentShields = maxShields
            
            // Speed settings impacted by engine level (upgraded engine allows higher base speed)
            val upgradeSpeedMultiplier = 1.0f + (snapshot.speedLevel - 1) * 0.12f
            baseScrollSpeed = 450f * carDef.baseSpeedMultiplier * upgradeSpeedMultiplier
            currentScrollSpeed = baseScrollSpeed
            speedMultiplier = 1.0f
            
            // Setup coordinates
            playerLane = 1
            playerX = 500f
            targetX = 500f
            playerY = 820f
            playerAngle = 0f
            oilSlideSpin = 0f
            spinOutTimer = 0f
            isInvulnerable = false
            invulnerabilityTimer = 0f
            screenShakeAmount = 0f
            
            // Invalidate gameplay records
            currentScore = 0
            coinsCollectedThisRun = 0
            gameTimeSec = 0f
            spawnTimer = 0f
            coinSpawnTimer = 0f
            levelProgressDistance = 0f
            nearMissesCount = 0
            timeTaken = 0f
            nearMissedObstacles.clear()
            
            // Empty game lists
            obstacles.clear()
            coins.clear()
            particles.clear()
            skidMarks.clear()
            
            // Pre-populate sidelines
            sidelineLeft.clear()
            sidelineRight.clear()
            for (i in 0..5) {
                spawnSidelineAtY(left = true, y = i * 200f - 100f)
                spawnSidelineAtY(left = false, y = i * 200f - 100f)
            }
            
            stopGameLoop()
            startGameLoop()
        }
    }

    private fun startGameLoop() {
        gameLoopJob = viewModelScope.launch {
            var lastTime = System.currentTimeMillis()
            while (currentScreen == GameState.PLAYING) {
                val now = System.currentTimeMillis()
                val elapsedMs = now - lastTime
                lastTime = now
                
                // Cap delta time to prevent giant leaps on lag spikes (60fps is ~16ms)
                val dt = (elapsedMs.coerceAtMost(50).toFloat()) / 1000f
                
                updateGameTick(dt)
                delay(16) // ~60fps target
            }
        }
    }

    private fun stopGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = null
    }

    // Heartbeat physics loop (updates every frame)
    private fun updateGameTick(dt: Float) {
        if (currentScreen != GameState.PLAYING) return
        
        if (tutorialTimerRemaining > 0f) {
            tutorialTimerRemaining = (tutorialTimerRemaining - dt).coerceAtLeast(0f)
            return
        }
        
        gameTimeSec += dt
        val snapshot = gameSaveState.value
        
        // Acceleration upgrade makes speed & multiplier ramp up much faster!
        val accelerationModifier = 1.0f + (snapshot.accelerationLevel - 1) * 0.15f
        speedMultiplier = 1.0f + ((gameTimeSec * accelerationModifier) / 50f).coerceAtMost(1.2f)
        currentScrollSpeed = baseScrollSpeed * speedMultiplier
        
        // Track level stage clear progress
        levelProgressDistance += currentScrollSpeed * dt
        if (levelProgressDistance >= 10000f) {
            triggerStageClear()
            return
        }
        
        // Accumulate active score based on scrolling speed (starting at 0 for each level)
        currentScore += (currentScrollSpeed * dt * 0.1f).toInt()
        
        // Parallax roadside dash line scroll
        dashLinesYOffset = (dashLinesYOffset + currentScrollSpeed * dt) % 150f
        
        // Shake dampening
        if (screenShakeAmount > 0) {
            screenShakeAmount = (screenShakeAmount - dt * 25f).coerceAtLeast(0f)
        }
        
        // Invincibility frame updates
        if (isInvulnerable) {
            invulnerabilityTimer -= dt
            if (invulnerabilityTimer <= 0) {
                isInvulnerable = false
            }
        }
        
        // Core Player horizontal physics interpolation (handling upgrade speed)
        val carDef = PixelSprites.UnlockedCarsList.getOrNull(snapshot.selectedCarId) ?: PixelSprites.UnlockedCarsList[0]
        val handlingCoeff = (0.08f + snapshot.handlingLevel * 0.03f) * carDef.baseHandlingMultiplier
        
        val diffX = targetX - playerX
        val motionVelocityX = diffX * (handlingCoeff * 100f)
        playerX += motionVelocityX * dt
        
        // Set visual tilt angle based on sideways motion
        val baseTilt = motionVelocityX * 0.04f
        playerAngle = if (spinOutTimer > 0f) {
            // Spin out rotation logic
            playerAngle + dt * 720f
        } else {
            // Smoothly ease back inline
            (baseTilt + oilSlideSpin).coerceIn(-18f, 18f)
        }
        
        // Wet oil slick recovering physics
        if (oilSlideSpin != 0f) {
            oilSlideSpin = if (oilSlideSpin > 0) {
                (oilSlideSpin - dt * 40f).coerceAtLeast(0f)
            } else {
                (oilSlideSpin + dt * 40f).coerceAtMost(0f)
            }
        }
        
        // Spin out recovery or game-over state terminal check
        if (spinOutTimer > 0f) {
            spinOutTimer -= dt
            if (spinOutTimer <= 0f && currentShields <= 0) {
                triggerGameOver()
                return
            }
        }

        // Particle dynamics
        val particleIterator = particles.iterator()
        while (particleIterator.hasNext()) {
            val p = particleIterator.next()
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.y += currentScrollSpeed * dt // Scroll particles downward along road
            p.life += dt
            if (p.life >= p.maxLife) {
                particles.remove(p)
                break
            }
        }
        
        // Horizontal road skid lines scroll and fade
        val skidIterator = skidMarks.iterator()
        while (skidIterator.hasNext()) {
            val s = skidIterator.next()
            s.y += currentScrollSpeed * dt
            s.opacity -= dt * 0.4f
            if (s.y > 1100f || s.opacity <= 0f) {
                skidMarks.remove(s)
                break
            }
        }

        // Sideline scenery scroll and recycling
        updateScenery(dt)

        // Spawning obstacles & items
        tickSpawners(dt)
        
        // Elements motion and collision
        updateObstaclesAndCheckCollisions(dt)
        updateCoinsCollection(dt)
    }

    private fun updateScenery(dt: Float) {
        // Left trees
        val leftIt = sidelineLeft.iterator()
        while (leftIt.hasNext()) {
            val obj = leftIt.next()
            obj.y += currentScrollSpeed * dt
            if (obj.y > 1100f) {
                sidelineLeft.remove(obj)
                spawnSidelineAtY(left = true, y = -150f)
                break
            }
        }
        
        // Right trees
        val rightIt = sidelineRight.iterator()
        while (rightIt.hasNext()) {
            val obj = rightIt.next()
            obj.y += currentScrollSpeed * dt
            if (obj.y > 1100f) {
                sidelineRight.remove(obj)
                spawnSidelineAtY(left = false, y = -150f)
                break
            }
        }
    }

    private fun spawnSidelineAtY(left: Boolean, y: Float) {
        val list = if (left) sidelineLeft else sidelineRight
        val allowed = activeTheme.sceneryTypes
        val type = if (allowed.isNotEmpty()) {
            allowed[Random.nextInt(allowed.size)]
        } else {
            "TREE"
        }
        val xOffset = if (left) {
            Random.nextFloat() * 120f + 20f // Range 20 to 140
        } else {
            Random.nextFloat() * 120f + 840f // Range 840 to 960
        }
        list.add(
            SidelineObject(
                type = type,
                isLeft = left,
                x = xOffset,
                y = y,
                scale = 0.8f + Random.nextFloat() * 0.4f,
                rotation = Random.nextFloat() * 360f
            )
        )
    }

    private fun tickSpawners(dt: Float) {
        // Obstacle Spawner tick rates (increases complexity with time)
        spawnTimer += dt
        val maxSpawnDelay = (1.8f - (gameTimeSec * 0.015f)).coerceAtLeast(0.75f)
        if (spawnTimer >= maxSpawnDelay) {
            spawnTimer = 0f
            spawnNewObstacle()
        }
        
        // Coin Spawner tick rates
        coinSpawnTimer += dt
        if (coinSpawnTimer >= 0.95f) {
            coinSpawnTimer = 0f
            spawnNewCoin()
        }
    }

    private fun spawnNewObstacle() {
        val laneIndex = Random.nextInt(3) // 0 (Left), 1 (Center), 2 (Right)
        
        // Check if there is already an obstacle or coin too close at top to avoid teleports
        val isBlocked = obstacles.any { it.lane == laneIndex && it.y < 150f }
        if (isBlocked) return
        
        val typesAvailable = mutableListOf(ObstacleType.BLUE_CAR, ObstacleType.ORANGE_CAR, ObstacleType.ROADBLOCK, ObstacleType.OIL_SPILL)
        
        // Add theme-specific obstacles
        when (activeTheme) {
            LevelTheme.DESERT_HIGHWAY -> {
                typesAvailable.add(ObstacleType.TUMBLEWEED)
                typesAvailable.add(ObstacleType.SAND_TRAP)
            }
            LevelTheme.ICY_PASS -> {
                typesAvailable.add(ObstacleType.ICE_PATCH)
                typesAvailable.add(ObstacleType.SNOW_DRIFT)
            }
            LevelTheme.URBAN_CITY -> {
                typesAvailable.add(ObstacleType.SPIKE_STRIP)
                typesAvailable.add(ObstacleType.TRAFFIC_CONE)
            }
            else -> {}
        }
        
        // Trucks spawn as game progresses beyond 15 seconds
        if (gameTimeSec > 15f) {
            typesAvailable.add(ObstacleType.TRUCK)
        }
        
        val selectedType = typesAvailable[Random.nextInt(typesAvailable.size)]
        
        // Custom physics bounds based on pixel grid drawing shapes
        val width: Float
        val height: Float
        val baseRelSpeed: Float
        
        when (selectedType) {
            ObstacleType.BLUE_CAR -> {
                width = 80f
                height = 120f
                baseRelSpeed = 80f // Move slightly slower, so player catches up smoothly
            }
            ObstacleType.ORANGE_CAR -> {
                width = 80f
                height = 120f
                baseRelSpeed = -50f // Coming fast / slowing down, triggers rapid response!
            }
            ObstacleType.TRUCK -> {
                width = 90f
                height = 200f
                baseRelSpeed = 160f // Slow moving heavy barrier
            }
            ObstacleType.ROADBLOCK -> {
                width = 120f
                height = 60f
                baseRelSpeed = 220f // Static on the road, must pass
            }
            ObstacleType.OIL_SPILL -> {
                width = 110f
                height = 90f
                baseRelSpeed = 220f // Static oil spill
            }
            ObstacleType.TUMBLEWEED -> {
                width = 70f
                height = 70f
                baseRelSpeed = 120f // moves sideways
            }
            ObstacleType.SAND_TRAP -> {
                width = 120f
                height = 100f
                baseRelSpeed = 220f // static trap
            }
            ObstacleType.ICE_PATCH -> {
                width = 110f
                height = 90f
                baseRelSpeed = 220f // static ice patch
            }
            ObstacleType.SNOW_DRIFT -> {
                width = 120f
                height = 80f
                baseRelSpeed = 220f // static snow mound
            }
            ObstacleType.SPIKE_STRIP -> {
                width = 130f
                height = 50f
                baseRelSpeed = 220f // static spike strip
            }
            ObstacleType.TRAFFIC_CONE -> {
                width = 60f
                height = 60f
                baseRelSpeed = 220f // static cone
            }
        }
        
        val startLaneX = when (laneIndex) {
            0 -> 300f
            1 -> 500f
            else -> 700f
        }

        // Slight horizontal shift within lanes for real feel
        val subLaneJitter = if (selectedType == ObstacleType.OIL_SPILL) {
            (Random.nextFloat() * 60f - 30f)
        } else {
            (Random.nextFloat() * 20f - 10f)
        }

        obstacles.add(
            Obstacle(
                id = System.nanoTime(),
                type = selectedType,
                lane = laneIndex,
                x = startLaneX + subLaneJitter,
                y = -180f,
                width = width,
                height = height,
                speedY = baseRelSpeed
            )
        )
    }

    private fun spawnNewCoin() {
        // Spawns coins in rows or clumps
        val laneIndex = Random.nextInt(3)
        val isBlocked = coins.any { it.lane == laneIndex && it.y < 100f }
        if (isBlocked) return
        
        val coinX = when (laneIndex) {
            0 -> 300f
            1 -> 500f
            else -> 700f
        } + (Random.nextFloat() * 40f - 20f)
        
        // Set coin value rolling probability based on coin upgrade levels
        val lvl = gameSaveState.value.coinValueLevel
        val goldChance = 0.05f + lvl * 0.05f
        val silverChance = 0.15f + lvl * 0.07f
        
        val roll = Random.nextFloat()
        val isGold = roll < goldChance
        val isSilver = !isGold && roll < (goldChance + silverChance)
        
        val amt = when {
            isGold -> 5
            isSilver -> 3
            else -> 1
        }
        
        coins.add(
            Coin(
                id = System.nanoTime(),
                lane = laneIndex,
                x = coinX,
                y = -100f,
                valAmount = amt,
                isGold = isGold,
                isSilver = isSilver
            )
        )
    }

    private fun updateObstaclesAndCheckCollisions(dt: Float) {
        val obsIterator = obstacles.iterator()
        while (obsIterator.hasNext()) {
            val obs = obsIterator.next()
            
            // Move relative speed scrolling down the field
            // Scrolling speed Y is aggregate speed (game background speed - relative obstacle frontwards speed)
            val scrollVelocityY = currentScrollSpeed - obs.speedY
            obs.y += scrollVelocityY * dt
            
            // Apply horizontal desert tumbleweed physics
            if (obs.type == ObstacleType.TUMBLEWEED) {
                val direction = if (obs.id % 2L == 0L) 1f else -1f
                obs.x += direction * 150f * dt
                if (obs.x < 220f) obs.x = 220f
                if (obs.x > 780f) obs.x = 780f
            }
            
            // Wipe off screen
            if (obs.y > 1150f) {
                obstacles.remove(obs)
                break
            }
            
            // Obstacle collision detection relative to player car hitbox
            if (obs.active && spinOutTimer <= 0f) {
                // Adjust strict smaller hitbox for precise/fair retro play
                val safePlayerW = 60f
                val safePlayerH = 100f
                
                val oW = obs.width * 0.82f
                val oH = obs.height * 0.82f
                
                val pLeft = playerX - safePlayerW / 2
                val pRight = playerX + safePlayerW / 2
                val pTop = playerY - safePlayerH / 2
                val pBottom = playerY + safePlayerH / 2
                
                val oLeft = obs.x - oW / 2
                val oRight = obs.x + oW / 2
                val oTop = obs.y - oH / 2
                val oBottom = obs.y + oH / 2
                
                // Box intersection check
                val isColliding = (pLeft < oRight && pRight > oLeft && pTop < oBottom && pBottom > oTop)
                
                if (isColliding) {
                    when (obs.type) {
                        ObstacleType.OIL_SPILL -> {
                            oilSlideSpin = if (Random.nextBoolean()) 40f else -40f
                            obs.active = false
                            emitPuddleSplashParticles(obs.x, obs.y)
                        }
                        ObstacleType.ICE_PATCH -> {
                            oilSlideSpin = if (Random.nextBoolean()) 80f else -80f
                            spinOutTimer = 0.8f // temporary steering block
                            obs.active = false
                            emitPuddleSplashParticles(obs.x, obs.y)
                        }
                        ObstacleType.SAND_TRAP -> {
                            currentScrollSpeed *= 0.4f
                            screenShakeAmount = 8f
                            if (!isInvulnerable) {
                                currentShields--
                                if (currentShields <= 0) {
                                    spinOutTimer = 1.0f
                                } else {
                                    isInvulnerable = true
                                    invulnerabilityTimer = 1.0f
                                }
                            }
                            obs.active = false
                            // Sand particles
                            repeat(8) {
                                particles.add(
                                    RetroParticle(
                                        x = obs.x, y = obs.y,
                                        vx = (Random.nextFloat() * 160f - 80f),
                                        vy = (Random.nextFloat() * 80f + 50f),
                                        color = androidx.compose.ui.graphics.Color(0xFFD4A373),
                                        size = 6f + Random.nextFloat() * 10f,
                                        life = 0f, maxLife = 0.4f
                                    )
                                )
                            }
                        }
                        ObstacleType.SNOW_DRIFT -> {
                            currentScrollSpeed *= 0.3f
                            screenShakeAmount = 12f
                            if (!isInvulnerable) {
                                currentShields--
                                if (currentShields <= 0) {
                                    spinOutTimer = 1.2f
                                } else {
                                    isInvulnerable = true
                                    invulnerabilityTimer = 1.2f
                                }
                            }
                            obs.active = false
                            // Snowy particles
                            repeat(12) {
                                particles.add(
                                    RetroParticle(
                                        x = obs.x, y = obs.y,
                                        vx = (Random.nextFloat() * 200f - 100f),
                                        vy = (Random.nextFloat() * 100f),
                                        color = androidx.compose.ui.graphics.Color.White,
                                        size = 6f + Random.nextFloat() * 12f,
                                        life = 0f, maxLife = 0.45f
                                    )
                                )
                            }
                        }
                        ObstacleType.SPIKE_STRIP -> {
                            screenShakeAmount = 18f
                            if (!isInvulnerable) {
                                currentShields--
                                if (currentShields <= 0) {
                                    spinOutTimer = 1.4f
                                } else {
                                    isInvulnerable = true
                                    invulnerabilityTimer = 1.4f
                                }
                            }
                            obs.active = false
                            emitCrashParticles(obs.x, obs.y)
                        }
                        ObstacleType.TRAFFIC_CONE -> {
                            currentScrollSpeed *= 0.85f
                            obs.active = false
                            // Oranges debris fragments
                            repeat(6) {
                                particles.add(
                                    RetroParticle(
                                        x = obs.x, y = obs.y,
                                        vx = (Random.nextFloat() * 180f - 90f),
                                        vy = (Random.nextFloat() * 100f - 50f),
                                        color = androidx.compose.ui.graphics.Color(0xFFFF5400),
                                        size = 5f + Random.nextFloat() * 8f,
                                        life = 0f, maxLife = 0.35f
                                    )
                                )
                            }
                        }
                        else -> {
                            if (!isInvulnerable) {
                                currentShields--
                                screenShakeAmount = 25f
                                emitCrashParticles(playerX, (playerY + obs.y) / 2f)
                                
                                if (currentShields <= 0) {
                                    spinOutTimer = 1.6f
                                } else {
                                    isInvulnerable = true
                                    invulnerabilityTimer = 1.5f
                                }
                                obs.active = false
                            }
                        }
                    }
                } else {
                    // Check near-misses
                    if (obs.type != ObstacleType.OIL_SPILL && obs.type != ObstacleType.ICE_PATCH) {
                        val dx = kotlin.math.abs(playerX - obs.x)
                        val dy = kotlin.math.abs(playerY - obs.y)
                        if (dy < 60f && dx < 130f && !nearMissedObstacles.contains(obs.id)) {
                            nearMissedObstacles.add(obs.id)
                            nearMissesCount++
                            currentScore += 500
                            
                            // Near miss sparks
                            repeat(6) {
                                particles.add(
                                    RetroParticle(
                                        x = (playerX + obs.x) / 2f,
                                        y = (playerY + obs.y) / 2f,
                                        vx = (Random.nextFloat() * 150f - 75f),
                                        vy = (Random.nextFloat() * 150f - 75f),
                                        color = androidx.compose.ui.graphics.Color(0xFF00E5FF),
                                        size = 5f + Random.nextFloat() * 8f,
                                        life = 0f,
                                        maxLife = 0.32f
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateCoinsCollection(dt: Float) {
        // Collect local variables to evaluate coin magnet capability
        val currProgress = gameSaveState.value
        // Magnet radius expands with coin value upgrades
        val magnetRadius = 110f + currProgress.coinValueLevel * 30f
        val pullForce = 350f + currProgress.coinValueLevel * 100f
        
        val coinIterator = coins.iterator()
        while (coinIterator.hasNext()) {
            val c = coinIterator.next()
            
            // Standard scroll down
            c.y += currentScrollSpeed * dt
            
            // Magnet pull physics towards vehicle if in radius
            val distToCar = sqrt((c.x - playerX).pow(2) + (c.y - playerY).pow(2))
            if (distToCar < magnetRadius && !c.collected && spinOutTimer <= 0f) {
                val dx = playerX - c.x
                val dy = playerY - c.y
                val ratioX = dx / distToCar
                val ratioY = dy / distToCar
                
                // Accelerate magnet pull force
                c.magneticPullX += ratioX * pullForce * dt
                c.magneticPullY += ratioY * pullForce * dt
                c.x += c.magneticPullX * dt
                c.y += c.magneticPullY * dt
            }
            
            // Filter off-screen
            if (c.y > 1150f) {
                coins.remove(c)
                break
            }
            
            // Coin hitbox check
            if (!c.collected && spinOutTimer <= 0f) {
                val hitDistance = sqrt((c.x - playerX).pow(2) + (c.y - playerY).pow(2))
                if (hitDistance < 65f) {
                    c.collected = true
                    coinsCollectedThisRun += c.valAmount
                    
                    // Add items collection score bonus
                    val coinScoreBonus = when {
                        c.isGold -> 500
                        c.isSilver -> 200
                        else -> 100
                    }
                    currentScore += coinScoreBonus
                    
                    // Trigger coin pickup sparkly star blast
                    emitCoinBursts(c.x, c.y, c.isGold, c.isSilver)
                }
            }
        }
    }

    private fun emitCoinBursts(x: Float, y: Float, isGold: Boolean, isSilver: Boolean) {
        val color = when {
            isGold -> PixelColors.Gold
            isSilver -> PixelColors.LightGrey
            else -> PixelColors.Yellow
        }
        for (i in 0..7) {
            particles.add(
                RetroParticle(
                    x = x,
                    y = y,
                    vx = (Random.nextFloat() * 180f - 90f),
                    vy = (Random.nextFloat() * 180f - 90f),
                    color = color,
                    size = 8f + Random.nextFloat() * 8f,
                    life = 0f,
                    maxLife = 0.35f
                )
            )
        }
    }

    private fun emitPuddleSplashParticles(x: Float, y: Float) {
        for (i in 0..10) {
            particles.add(
                RetroParticle(
                    x = x,
                    y = y,
                    vx = (Random.nextFloat() * 200f - 100f),
                    vy = (Random.nextFloat() * 100f - 50f),
                    color = PixelColors.Grey,
                    size = 5f + Random.nextFloat() * 12f,
                    life = 0f,
                    maxLife = 0.4f
                )
            )
        }
    }

    private fun emitCrashParticles(x: Float, y: Float) {
        // High fidelity sparks, smoke elements, and fireballs
        repeat(15) {
            particles.add(
                RetroParticle(
                    x = x + Random.nextFloat() * 40f - 20f,
                    y = y + Random.nextFloat() * 40f - 20f,
                    vx = (Random.nextFloat() * 300f - 150f),
                    vy = (Random.nextFloat() * 300f - 150f),
                    color = when (Random.nextInt(3)) {
                        0 -> PixelColors.Orange
                        1 -> PixelColors.Yellow
                        else -> PixelColors.DarkGrey
                    },
                    size = 12f + Random.nextFloat() * 16f,
                    life = 0f,
                    maxLife = 0.62f
                )
            )
        }
    }

    private fun triggerGameOver() {
        stopGameLoop()
        navigateTo(GameState.GAME_OVER)
        
        // Write the run outcomes to database
        viewModelScope.launch {
            val progress = repository.getGameSave()
            val finalCoins = progress.coins + coinsCollectedThisRun
            var finalHighScore = progress.highScore
            if (currentScore > finalHighScore) {
                finalHighScore = currentScore
            }
            repository.saveProgress(
                progress.copy(
                    coins = finalCoins,
                    highScore = finalHighScore
                )
            )
        }
    }

    private fun triggerStageClear() {
        stopGameLoop()
        navigateTo(GameState.STAGE_CLEAR)
        
        viewModelScope.launch {
            val progress = repository.getGameSave()
            
            // Add collected coins to purse
            val finalCoins = progress.coins + coinsCollectedThisRun
            
            // Apply bonus score for fast completion: Time bonus
            val timeBonus = ((60f - gameTimeSec).coerceAtLeast(0f) * 150f).toInt()
            currentScore += timeBonus
            
            var finalHighScore = progress.highScore
            if (currentScore > finalHighScore) {
                finalHighScore = currentScore
            }
            
            repository.saveProgress(
                progress.copy(
                    coins = finalCoins,
                    highScore = finalHighScore
                )
            )
        }
    }

    fun cycleTheme(forward: Boolean) {
        val allThemes = LevelTheme.values()
        val currentIndex = allThemes.indexOf(activeTheme)
        val nextIndex = if (forward) {
            (currentIndex + 1) % allThemes.size
        } else {
            (currentIndex - 1 + allThemes.size) % allThemes.size
        }
        activeTheme = allThemes[nextIndex]
    }

    fun selectLanguage(langCode: String) {
        viewModelScope.launch {
            val progress = repository.getGameSave()
            if (progress.language != langCode) {
                repository.saveProgress(progress.copy(language = langCode))
            }
        }
    }
}
