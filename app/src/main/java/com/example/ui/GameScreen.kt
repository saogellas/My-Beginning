package com.example.ui

import com.example.data.GameSave
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

import androidx.compose.ui.text.font.FontStyle

// High-fidelity custom pixel palette for retro borders, texts and Bento Grid theme
object GamingColors {
    val DeepPurple = Color(0xFF0F0F0F) // Bento main dark background
    val ArcadeBoard = Color(0xFF1D1B20) // Bento dark card surface
    val ScreenCap = Color(0xFF0F0F0F) // Bento screen dark canvas
    val LedText = Color(0xFFD0BCFF) // Bento Neon Lavender
    val Crimson = Color(0xFFF2B8B5) // Bento soft coral red
    val CyberGold = Color(0xFFFFD700) // Neon gold
    val NeonBlue = Color(0xFF00E5FF) // Neon Cyan
    val SteelBorder = Color(0xFF333333) // Bento grid line / fine border
    val RoadGray = Color(0xFF1E1E1E) // Darker highway surface
    val GrassGreen = Color(0xFF0F0F0F) // Clean dark background grid side fields
    val GrassLight = Color(0xFF222222) // Minimal dark accent
    val CurbRed = Color(0xFFFF0055) // Active curb marker red
    val CurbWhite = Color(0xFFD0BCFF) // Active curb marker lavender/white
    
    // Bento Specific Theme Colors
    val BentoLightCard = Color(0xFFEADDFF)
    val BentoDarkCard = Color(0xFF1D1B20)
    val BentoMediumCard = Color(0xFF332D41)
    val BentoTextDark = Color(0xFF21005D)
    val BentoTextLight = Color(0xFFD0BCFF)
    val BentoBorder = Color(0xFF49454F)
    val ActiveGreen = Color(0xFF81C784)
}

@Composable
fun PixelSpriteImage(
    sprite: List<String>,
    pixelSize: Float = 3f,
    modifier: Modifier = Modifier,
    colorMap: Map<Char, Color> = PixelSprites.DefaultColorMap
) {
    val rows = sprite.size
    val cols = sprite[0].length
    Canvas(modifier = modifier.size((cols * pixelSize).dp, (rows * pixelSize).dp)) {
        drawPixelSprite(
            sprite = sprite,
            centerX = size.width / 2f,
            centerY = size.height / 2f,
            pixelSize = pixelSize,
            colorMap = colorMap
        )
    }
}

fun Modifier.bentoGridBackground(): Modifier = this.drawBehind {
    val gridSize = 24.dp.toPx()
    val dotRadius = 1.2f * density
    val dotColor = Color.White.copy(alpha = 0.08f)
    var x = 0f
    while (x < size.width) {
        var y = 0f
        while (y < size.height) {
            drawCircle(
                color = dotColor,
                radius = dotRadius,
                center = Offset(x, y)
            )
            y += gridSize
        }
        x += gridSize
    }
}

@Composable
fun BentoUpgradeCardLight(
    name: String,
    currentLvl: Int,
    cost: Int,
    canAfford: Boolean,
    onUpgrade: () -> Unit,
    testTagPrefix: String,
    lang: String = "en"
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(145.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(GamingColors.BentoLightCard)
            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                val resolvedTitle = when (name) {
                    "Engine Tuning" -> Localization.loc("engine_tuning", lang).uppercase()
                    "Nitrogen Booster" -> Localization.loc("nitrogen_booster", lang).uppercase()
                    else -> name.uppercase()
                }
                Text(
                    text = resolvedTitle,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = GamingColors.BentoTextDark.copy(alpha = 0.6f),
                    fontSize = 9.sp
                )
                Text(
                    text = "LVL 0$currentLvl",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    color = GamingColors.BentoTextDark,
                    fontSize = 18.sp
                )
            }

            // Progress bar style level indicators
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(GamingColors.BentoTextDark.copy(alpha = 0.15f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(currentLvl / 5f)
                        .clip(RoundedCornerShape(50.dp))
                        .background(GamingColors.BentoTextDark)
                )
            }

            // Upgrade button
            if (currentLvl >= 5) {
                Text(
                    text = Localization.loc("upgrade_full", lang).uppercase(),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GamingColors.BentoTextDark
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black)
                ) {
                    Button(
                        onClick = onUpgrade,
                        enabled = canAfford,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            disabledContainerColor = Color.White.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 2.dp)
                            .testTag("btn_upgrade_${testTagPrefix}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = GamingColors.CyberGold, modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                    text = "$cost",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = GamingColors.BentoTextDark
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BentoUpgradeCardDark(
    name: String,
    currentLvl: Int,
    cost: Int,
    canAfford: Boolean,
    onUpgrade: () -> Unit,
    testTagPrefix: String,
    lang: String = "en"
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(145.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(GamingColors.BentoDarkCard)
            .border(1.dp, GamingColors.BentoBorder, RoundedCornerShape(24.dp))
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = Localization.loc("chassis_armor", lang).uppercase(),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = GamingColors.BentoTextLight,
                    fontSize = 9.sp
                )
                Text(
                    text = "LVL 0$currentLvl",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    fontSize = 18.sp
                )
            }

            // 5 indicator dots
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (i in 1..5) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(50.dp))
                            .background(if (i <= currentLvl) GamingColors.NeonBlue else GamingColors.BentoBorder)
                    )
                }
            }

            // Button
            if (currentLvl >= 5) {
                Text(
                    text = Localization.loc("upgrade_full", lang).uppercase(),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GamingColors.NeonBlue
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black)
                ) {
                    Button(
                        onClick = onUpgrade,
                        enabled = canAfford,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (canAfford) Color(0xFF332D41) else Color.Gray.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 2.dp)
                            .testTag("btn_upgrade_${testTagPrefix}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = GamingColors.CyberGold, modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                    text = "$cost",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (canAfford) Color.White else Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BentoUpgradeCardMedium(
    name: String,
    currentLvl: Int,
    cost: Int,
    canAfford: Boolean,
    onUpgrade: () -> Unit,
    testTagPrefix: String,
    lang: String = "en"
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(145.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(GamingColors.BentoMediumCard)
            .border(1.dp, GamingColors.BentoBorder, RoundedCornerShape(24.dp))
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = Localization.loc("tire_traction", lang).uppercase(),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = GamingColors.BentoTextLight,
                    fontSize = 9.sp
                )
                Text(
                    text = "LVL 0$currentLvl",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    fontSize = 18.sp
                )
            }

            // custom bar
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                for (i in 1..5) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(50.dp))
                            .background(if (i <= currentLvl) GamingColors.CyberGold else GamingColors.BentoBorder)
                    )
                }
            }

            // Button
            if (currentLvl >= 5) {
                Text(
                    text = Localization.loc("upgrade_full", lang).uppercase(),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GamingColors.CyberGold
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black)
                ) {
                    Button(
                        onClick = onUpgrade,
                        enabled = canAfford,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (canAfford) Color(0xFF1D1B20) else Color.Gray.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 2.dp)
                            .testTag("btn_upgrade_${testTagPrefix}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = GamingColors.CyberGold, modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                    text = "$cost",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (canAfford) Color.White else Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BentoUpgradeCardDarkAlt(
    name: String,
    currentLvl: Int,
    cost: Int,
    canAfford: Boolean,
    onUpgrade: () -> Unit,
    testTagPrefix: String,
    lang: String = "en"
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(145.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(GamingColors.BentoDarkCard)
            .border(1.dp, GamingColors.BentoBorder, RoundedCornerShape(24.dp))
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = Localization.loc("coin_harvester", lang).uppercase(),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = GamingColors.BentoTextLight,
                    fontSize = 9.sp
                )
                Text(
                    text = "LVL 0$currentLvl",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    fontSize = 18.sp
                )
            }

            // 5 level bars
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                for (i in 1..5) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(50.dp))
                            .background(if (i <= currentLvl) GamingColors.ActiveGreen else GamingColors.BentoBorder)
                    )
                }
            }

            // Button
            if (currentLvl >= 5) {
                Text(
                    text = Localization.loc("upgrade_full", lang).uppercase(),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GamingColors.ActiveGreen
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black)
                ) {
                    Button(
                        onClick = onUpgrade,
                        enabled = canAfford,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (canAfford) Color(0xFFEADDFF) else Color.Gray.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 2.dp)
                            .testTag("btn_upgrade_${testTagPrefix}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = if (canAfford) GamingColors.BentoTextDark else Color.Gray, modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                    text = "$cost",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (canAfford) GamingColors.BentoTextDark else Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GameScreen(viewModel: GameViewModel) {
    val currentScreen = viewModel.currentScreen
    val gameSave by viewModel.gameSaveState.collectAsState()

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        color = GamingColors.DeepPurple
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (currentScreen) {
                GameState.MENU -> MenuLayout(viewModel, gameSave)
                GameState.SHOP -> UpgradeShopLayout(viewModel, gameSave)
                GameState.PLAYING -> GameplayLayout(viewModel, gameSave)
                GameState.GAME_OVER -> GameOverLayout(viewModel, gameSave)
                GameState.STAGE_CLEAR -> StageClearLayout(viewModel, gameSave)
                GameState.SETTINGS -> SettingsLayout(viewModel, gameSave)
            }
        }
    }
}

// --------------------- CORE GAMEPLAY SCREEN --------------------- //
@Composable
fun GameplayLayout(viewModel: GameViewModel, gameSave: GameSave) {
    val activeCar = PixelSprites.UnlockedCarsList.getOrNull(gameSave.selectedCarId) ?: PixelSprites.UnlockedCarsList[0]
    val shakeAmt = viewModel.screenShakeAmount
    
    // Virtual drawing frame variables
    val leftRoadBoundary = 200f
    val rightRoadBoundary = 800f
    val roadWidth = rightRoadBoundary - leftRoadBoundary
    
    // We animate a neon flash for invulnerability
    val infiniteTransition = rememberInfiniteTransition(label = "flash")
    val alphaFlash by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(120, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flash_alpha"
    )

    // Shield spinning rotation
    val shieldRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shield_spin"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(GamingColors.ScreenCap)
        ) {
        // Interactive Bento HUD top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(GamingColors.ScreenCap)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Life block (left)
            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(GamingColors.BentoDarkCard)
                    .border(1.dp, GamingColors.BentoBorder, RoundedCornerShape(16.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Shield health",
                        tint = Color(0xFFF2B8B5),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = (Localization.loc("shield", gameSave.language) + " ").uppercase(),
                        fontFamily = FontFamily.Monospace,
                        color = GamingColors.BentoTextLight,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                        repeat(viewModel.maxShields) { idx ->
                            Box(
                                modifier = Modifier
                                    .size(width = 8.dp, height = 8.dp)
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(if (idx < viewModel.currentShields) GamingColors.NeonBlue else Color.Gray.copy(alpha = 0.3f))
                            )
                        }
                    }
                }
            }

            // Score indicator block (center)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(GamingColors.BentoDarkCard)
                    .border(1.dp, GamingColors.BentoBorder, RoundedCornerShape(16.dp))
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = Localization.loc("score", gameSave.language).uppercase(),
                        fontFamily = FontFamily.Monospace,
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = String.format("%06d", viewModel.currentScore),
                        fontFamily = FontFamily.Monospace,
                        color = GamingColors.NeonBlue,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.testTag("current_score")
                    )
                }
            }

            // Coins collected (right)
            Box(
                modifier = Modifier
                    .weight(0.9f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(GamingColors.BentoDarkCard)
                    .border(1.dp, GamingColors.BentoBorder, RoundedCornerShape(16.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Coins",
                        tint = GamingColors.CyberGold,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${viewModel.coinsCollectedThisRun}",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = GamingColors.CyberGold,
                        fontSize = 13.sp,
                        modifier = Modifier.testTag("run_coins_collected")
                    )
                }
            }
        }

        // Theme and level progress indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = viewModel.activeTheme.displayName.uppercase(),
                color = Color.LightGray,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.width(115.dp)
            )
            val progressPercentScale = (viewModel.levelProgressDistance / 10000f).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.Gray.copy(alpha = 0.2f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progressPercentScale)
                        .background(viewModel.activeTheme.curbColor1)
                )
            }
            Text(
                text = "${(progressPercentScale * 100).toInt()}%",
                color = viewModel.activeTheme.curbColor1,
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(36.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )
        }

        // Parallax Interactive Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.Black)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("gameplay_canvas")
            ) {
                // Read frame width and scale to match Virtual Coordinate System (1000x1000)
                val canvasW = size.width
                val canvasH = size.height
                
                val scaleX = canvasW / 1000f
                val scaleY = canvasH / 1000f
                
                // Screen shake physics displacement
                val shakeOffsetX = if (shakeAmt > 0) (Random.nextFloat() * shakeAmt * 2f - shakeAmt) else 0f
                val shakeOffsetY = if (shakeAmt > 0) (Random.nextFloat() * shakeAmt * 2f - shakeAmt) else 0f

                withTransform({
                    // Scale drawing matrix to fit Virtual Boundaries
                    scale(scaleX, scaleY, Offset.Zero)
                    translate(shakeOffsetX, shakeOffsetY)
                }) {
                    
                     // 1. Draw Sideline Grass Background fields
                    // Left grass
                    drawRect(
                        color = viewModel.activeTheme.grassColor,
                        topLeft = Offset(0f, 0f),
                        size = Size(leftRoadBoundary, 1000f)
                    )
                    // Right grass
                    drawRect(
                        color = viewModel.activeTheme.grassColor,
                        topLeft = Offset(rightRoadBoundary, 0f),
                        size = Size(1000f - rightRoadBoundary, 1000f)
                    )
                    
                    // 2. Center Highway Asphalt Road
                    drawRect(
                        color = viewModel.activeTheme.roadColor,
                        topLeft = Offset(leftRoadBoundary, 0f),
                        size = Size(roadWidth, 1000f)
                    )
                    
                    // 3. Red & White Curbs scrolling on sides
                    val segmentLength = 80f
                    var cursorY = -150f + (viewModel.dashLinesYOffset % segmentLength)
                    while (cursorY < 1050f) {
                        val stripeColor = if (((cursorY / segmentLength).toInt() % 2) == 0) {
                            viewModel.activeTheme.curbColor1
                        } else {
                            viewModel.activeTheme.curbColor2
                        }
                        // Left curb
                        drawRect(
                            color = stripeColor,
                            topLeft = Offset(leftRoadBoundary - 12f, cursorY),
                            size = Size(12f, segmentLength)
                        )
                        // Right curb
                        drawRect(
                            color = stripeColor,
                            topLeft = Offset(rightRoadBoundary, cursorY),
                            size = Size(12f, segmentLength)
                        )
                        cursorY += segmentLength
                    }

                    // 4. White Dash Lane Separators
                    // Separator 1 at x = 400, Separator 2 at x = 600
                    val dashHeight = 50f
                    val dashGap = 60f
                    val dashUnitHeight = dashHeight + dashGap
                    
                    var dashY = -150f + (viewModel.dashLinesYOffset % dashUnitHeight)
                    while (dashY < 1050f) {
                        // Lane divide 1
                        drawRect(
                            color = viewModel.activeTheme.dashColor,
                            topLeft = Offset(398f, dashY),
                            size = Size(4f, dashHeight)
                        )
                        // Lane divide 2
                        drawRect(
                            color = viewModel.activeTheme.dashColor,
                            topLeft = Offset(598f, dashY),
                            size = Size(4f, dashHeight)
                        )
                        dashY += dashUnitHeight
                    }

                    // 5. Draw roadside grass details (Trees, rocks, etc.)
                    viewModel.sidelineLeft.forEach { item ->
                        drawSceneryObject(item)
                    }
                    viewModel.sidelineRight.forEach { item ->
                        drawSceneryObject(item)
                    }

                    // 6. Draw tyre skid marks left behind
                    viewModel.skidMarks.forEach { xMark ->
                        drawRect(
                            color = Color.Black.copy(alpha = xMark.opacity.coerceIn(0f, 1f)),
                            topLeft = Offset(xMark.x - 22f, xMark.y),
                            size = Size(8f, 35f)
                        )
                        drawRect(
                            color = Color.Black.copy(alpha = xMark.opacity.coerceIn(0f, 1f)),
                            topLeft = Offset(xMark.x + 14f, xMark.y),
                            size = Size(8f, 35f)
                        )
                    }

                    // 7. Draw road details (oil puddle splatters, static roadblocks)
                    viewModel.obstacles.forEach { obs ->
                        if (!obs.active) return@forEach
                        
                        when (obs.type) {
                            ObstacleType.BLUE_CAR -> {
                                drawPixelSprite(
                                    sprite = PixelSprites.ObstacleBlueCar,
                                    centerX = obs.x,
                                    centerY = obs.y,
                                    pixelSize = 8.5f,
                                    rotation = obs.angle
                                )
                            }
                            ObstacleType.ORANGE_CAR -> {
                                drawPixelSprite(
                                    sprite = PixelSprites.ObstacleOrangeCar,
                                    centerX = obs.x,
                                    centerY = obs.y,
                                    pixelSize = 8.5f,
                                    rotation = obs.angle
                                )
                            }
                            ObstacleType.TRUCK -> {
                                drawPixelSprite(
                                    sprite = PixelSprites.ObstacleTruck,
                                    centerX = obs.x,
                                    centerY = obs.y,
                                    pixelSize = 8.5f,
                                    rotation = obs.angle
                                )
                            }
                            ObstacleType.ROADBLOCK -> {
                                drawPixelSprite(
                                    sprite = PixelSprites.ObstacleRoadblock,
                                    centerX = obs.x,
                                    centerY = obs.y,
                                    pixelSize = 8f,
                                    rotation = obs.angle
                                )
                            }
                            ObstacleType.OIL_SPILL -> {
                                drawPixelSprite(
                                    sprite = PixelSprites.ObstacleOilSpill,
                                    centerX = obs.x,
                                    centerY = obs.y,
                                    pixelSize = 10f,
                                    rotation = obs.angle
                                )
                            }
                            ObstacleType.TUMBLEWEED -> {
                                drawPixelSprite(
                                    sprite = PixelSprites.ObstacleTumbleweed,
                                    centerX = obs.x,
                                    centerY = obs.y,
                                    pixelSize = 8f,
                                    rotation = obs.angle
                                )
                            }
                            ObstacleType.SAND_TRAP -> {
                                drawPixelSprite(
                                    sprite = PixelSprites.ObstacleSandTrap,
                                    centerX = obs.x,
                                    centerY = obs.y,
                                    pixelSize = 8.5f,
                                    rotation = obs.angle
                                )
                            }
                            ObstacleType.ICE_PATCH -> {
                                drawPixelSprite(
                                    sprite = PixelSprites.ObstacleIcePatch,
                                    centerX = obs.x,
                                    centerY = obs.y,
                                    pixelSize = 8.5f,
                                    rotation = obs.angle
                                )
                            }
                            ObstacleType.SNOW_DRIFT -> {
                                drawPixelSprite(
                                    sprite = PixelSprites.ObstacleSnowDrift,
                                    centerX = obs.x,
                                    centerY = obs.y,
                                    pixelSize = 8f,
                                    rotation = obs.angle
                                )
                            }
                            ObstacleType.SPIKE_STRIP -> {
                                drawPixelSprite(
                                    sprite = PixelSprites.ObstacleSpikeStrip,
                                    centerX = obs.x,
                                    centerY = obs.y,
                                    pixelSize = 8.5f,
                                    rotation = obs.angle
                                )
                            }
                            ObstacleType.TRAFFIC_CONE -> {
                                drawPixelSprite(
                                    sprite = PixelSprites.ObstacleTrafficCone,
                                    centerX = obs.x,
                                    centerY = obs.y,
                                    pixelSize = 8f,
                                    rotation = obs.angle
                                )
                            }
                        }
                    }

                    // 8. General Golden / Silver Coins
                    viewModel.coins.forEach { coin ->
                        if (coin.collected) return@forEach
                        val coinColorMap = if (coin.isGold) {
                            mapOf('.' to Color.Transparent, 'k' to PixelColors.Black, 's' to PixelColors.Gold, 'w' to Color.White)
                        } else if (coin.isSilver) {
                            mapOf('.' to Color.Transparent, 'k' to PixelColors.Black, 's' to PixelColors.LightGrey, 'w' to Color.White)
                        } else {
                            mapOf('.' to Color.Transparent, 'k' to PixelColors.Black, 's' to Color(0xFFCD7F32), 'w' to Color.White) // Bronze
                        }
                        
                        drawPixelSprite(
                            sprite = PixelSprites.CoinStar,
                            centerX = coin.x,
                            centerY = coin.y,
                            pixelSize = 7.5f,
                            colorMap = coinColorMap
                        )
                    }

                    // 9. Retro Spark and Explosion system
                    viewModel.particles.forEach { part ->
                        drawRect(
                            color = part.color.copy(alpha = (1f - (part.life / part.maxLife)).coerceIn(0f, 1f)),
                            topLeft = Offset(part.x - part.size / 2f, part.y - part.size / 2f),
                            size = Size(part.size, part.size)
                        )
                    }

                    // 10. Draw Driven Pixel Car
                    val flashTint = viewModel.isInvulnerable && alphaFlash < 0.6f
                    
                    drawPixelSprite(
                        sprite = activeCar.spriteRaw,
                        centerX = viewModel.playerX,
                        centerY = viewModel.playerY,
                        pixelSize = 8.5f,
                        rotation = viewModel.playerAngle,
                        flashTransparent = flashTint
                    )

                    // 11. Draw Custom Active Glowing Forcefield Shield
                    if (viewModel.currentShields > 1 && viewModel.spinOutTimer <= 0f) {
                        withTransform({
                            rotate(shieldRotation, Offset(viewModel.playerX, viewModel.playerY))
                        }) {
                            drawCircle(
                                color = GamingColors.NeonBlue.copy(alpha = 0.35f),
                                radius = 70f,
                                center = Offset(viewModel.playerX, viewModel.playerY),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                    width = 6f,
                                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                                        floatArrayOf(30f, 20f), 0f
                                    )
                                )
                            )
                        }
                    }
                }
            }
        }

        // Real-time Bento footer stats
        val hullPercent = if (viewModel.maxShields > 0) (viewModel.currentShields * 100 / viewModel.maxShields) else 100
        val currentSpeedKmh = (120f + (viewModel.currentScore / 200f).coerceAtMost(160f)).toInt()
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "HULL INTEGRITY: $hullPercent%",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
            Text(
                text = "SPEED: $currentSpeedKmh KM/H",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
        }

        // Bento Highway steer controls deck (dual column navigation block)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F0F0F))
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .windowInsetsPadding(WindowInsets.navigationBars),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bento Steer Left Button (3D)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(72.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.Black)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 4.dp)
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 24.dp, bottomEnd = 24.dp))
                        .background(Color(0xFF2B2930))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = GamingColors.CyberGold, bounded = true)
                        ) {
                            viewModel.moveLaneLeft()
                        }
                        .testTag("key_arrow_left"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Steer left lane",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            // Bento Steer Right Button (3D)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(72.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.Black)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 4.dp)
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 24.dp, bottomEnd = 24.dp))
                        .background(Color(0xFF2B2930))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = GamingColors.CyberGold, bounded = true)
                        ) {
                            viewModel.moveLaneRight()
                        }
                        .testTag("key_arrow_right"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Steer right lane",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }

    // --- TUTORIAL OVERLAY SCREEN ---
        val tutorialRemaining = viewModel.tutorialTimerRemaining
        if (tutorialRemaining > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { viewModel.skipTutorial() }
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 480.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(GamingColors.BentoDarkCard)
                        .border(2.dp, GamingColors.NeonBlue, RoundedCornerShape(24.dp))
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Localization.loc("tutorial_title", gameSave.language).uppercase(),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = GamingColors.NeonBlue
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(GamingColors.CurbRed)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "LIVE",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                fontSize = 8.sp,
                                color = Color.White
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val progressFraction = (tutorialRemaining / 5.0f).coerceIn(0f, 1f)
                        val secondsLeft = kotlin.math.ceil(tutorialRemaining).toInt()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = Localization.loc("tutorial_start_in", gameSave.language).replace("%s", secondsLeft.toString()).uppercase(),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color.White.copy(alpha = 0.1f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(progressFraction)
                                    .background(GamingColors.CyberGold)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black.copy(alpha = 0.4f))
                            .border(1.dp, GamingColors.BentoBorder, RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(GamingColors.CyberGold.copy(alpha = 0.15f))
                                    .border(1.dp, GamingColors.CyberGold, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                PixelSpriteImage(
                                    sprite = PixelSprites.CoinStar,
                                    pixelSize = 3.5f
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = Localization.loc("tutorial_to_take", gameSave.language).uppercase(),
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = GamingColors.CyberGold
                                )
                                Text(
                                    text = Localization.loc("tutorial_take_desc", gameSave.language),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black.copy(alpha = 0.4f))
                            .border(1.dp, GamingColors.BentoBorder, RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(GamingColors.CurbRed.copy(alpha = 0.15f))
                                    .border(1.dp, GamingColors.CurbRed, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    PixelSpriteImage(
                                        sprite = PixelSprites.ObstacleRoadblock,
                                        pixelSize = 1.6f
                                    )
                                }
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = Localization.loc("tutorial_to_avoid", gameSave.language).uppercase(),
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = GamingColors.CurbRed
                                )
                                Text(
                                    text = Localization.loc("tutorial_avoid_desc", gameSave.language),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    val infiniteTapTransition = rememberInfiniteTransition(label = "pulse")
                    val pulseAlpha by infiniteTapTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1.0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulse_alpha"
                    )

                    Button(
                        onClick = { viewModel.skipTutorial() },
                        colors = ButtonDefaults.buttonColors(containerColor = GamingColors.NeonBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = Localization.loc("tutorial_skip", gameSave.language).uppercase(),
                            color = Color.Black,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            modifier = Modifier.graphicsLayer { alpha = pulseAlpha }
                        )
                    }
                }
            }
        }
    }
}

// Procedural visual rendering mapping for greenery objects
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSceneryObject(
    obj: SidelineObject
) {
    val sizePx = 35f * obj.scale
    when (obj.type) {
        "TREE" -> {
            // Draw a cartoonish pixel tree (circles representing leaf bundle layer)
            drawCircle(
                color = PixelColors.Brown,
                radius = 8f * obj.scale,
                center = Offset(obj.x, obj.y + 16f)
            )
            drawCircle(
                color = GamingColors.GrassLight,
                radius = 20f * obj.scale,
                center = Offset(obj.x, obj.y)
            )
            drawCircle(
                color = GamingColors.GrassGreen,
                radius = 14f * obj.scale,
                center = Offset(obj.x - 6f, obj.y - 6f)
            )
        }
        "CACTUS" -> {
            // Cactus grid
            drawRect(
                color = PixelColors.Green,
                topLeft = Offset(obj.x - 5f, obj.y - 15f),
                size = Size(10f, 35f)
            )
            drawRect(
                color = PixelColors.Green,
                topLeft = Offset(obj.x - 18f, obj.y - 5f),
                size = Size(13f, 8f)
            )
            drawRect(
                color = PixelColors.Green,
                topLeft = Offset(obj.x - 18f, obj.y - 12f),
                size = Size(8f, 10f)
            )
            drawRect(
                color = PixelColors.Green,
                topLeft = Offset(obj.x + 5f, obj.y + 2f),
                size = Size(13f, 8f)
            )
            drawRect(
                color = PixelColors.Green,
                topLeft = Offset(obj.x + 10f, obj.y - 8f),
                size = Size(8f, 12f)
            )
        }
        "ROCK" -> {
            // Layered rocks
            drawCircle(
                color = PixelColors.DarkGrey,
                radius = 16f * obj.scale,
                center = Offset(obj.x, obj.y)
            )
            drawCircle(
                color = PixelColors.Grey,
                radius = 12f * obj.scale,
                center = Offset(obj.x - 4f, obj.y - 4f)
            )
        }
        else -> { // FLOWER
            drawCircle(
                color = PixelColors.Brown,
                radius = 5f * obj.scale,
                center = Offset(obj.x, obj.y)
            )
            // Yellow petals
            drawCircle(color = PixelColors.Gold, radius = 5f * obj.scale, center = Offset(obj.x - 8f, obj.y))
            drawCircle(color = PixelColors.Gold, radius = 5f * obj.scale, center = Offset(obj.x + 8f, obj.y))
            drawCircle(color = PixelColors.Gold, radius = 5f * obj.scale, center = Offset(obj.x, obj.y - 8f))
            drawCircle(color = PixelColors.Gold, radius = 5f * obj.scale, center = Offset(obj.x, obj.y + 8f))
        }
    }
}


// --------------------- RETRO PLAY MAIN MENU --------------------- //
@Composable
fun MenuLayout(viewModel: GameViewModel, gameSave: GameSave) {
    val activeCar = PixelSprites.UnlockedCarsList.getOrNull(gameSave.selectedCarId) ?: PixelSprites.UnlockedCarsList[0]
    
    // We render a simple retro-futurist skyline grid on menu layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GamingColors.ScreenCap)
            .bentoGridBackground() // Blueprint background dots
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.height(20.dp))
            
            // Bento-style Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = Localization.loc("course_subtitle", gameSave.language),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = GamingColors.BentoTextLight.copy(alpha = 0.8f),
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = Localization.loc("app_title", gameSave.language),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic,
                        color = Color.White,
                        letterSpacing = (-0.5).sp,
                        modifier = Modifier.testTag("app_title")
                    )
                }
                
                // Total coins purse in active-capsule shape
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(GamingColors.BentoDarkCard)
                        .border(1.dp, GamingColors.BentoBorder, RoundedCornerShape(50.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pulse Green Dot
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(GamingColors.ActiveGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Coins",
                        tint = GamingColors.CyberGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${gameSave.coins}",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.White,
                        modifier = Modifier.testTag("total_coins_value")
                    )
                }
            }

            // Bento Highway Viewport Block
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .border(1.dp, GamingColors.BentoBorder, RoundedCornerShape(32.dp))
                    .background(GamingColors.BentoDarkCard)
                    .bentoGridBackground(), // Overlay grid inside view
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize().padding(1.dp)) {
                    // Background highway demo render with generous lane width
                    val leftBoundary = size.width * 0.25f
                    val rightBoundary = size.width * 0.75f
                    val rdWidth = rightBoundary - leftBoundary
                    
                    // Draw road asphalt
                    drawRect(color = GamingColors.RoadGray, topLeft = Offset(leftBoundary, 0f), size = Size(rdWidth, size.height))
                    
                    // Draw curbs
                    drawRect(color = GamingColors.CurbRed, topLeft = Offset(leftBoundary - 10f, 0f), size = Size(10f, size.height))
                    drawRect(color = GamingColors.CurbRed, topLeft = Offset(rightBoundary, 0f), size = Size(10f, size.height))
                    
                    // Draw dotted lane marks
                    val laneStripeH = 30f
                    var stripeY = 0f
                    while (stripeY < size.height) {
                        drawRect(
                            color = Color.White.copy(alpha = 0.25f),
                            topLeft = Offset(leftBoundary + rdWidth / 3f, stripeY),
                            size = Size(4f, laneStripeH)
                        )
                        drawRect(
                            color = Color.White.copy(alpha = 0.25f),
                            topLeft = Offset(leftBoundary + 2f * rdWidth / 3f, stripeY),
                            size = Size(4f, laneStripeH)
                        )
                        stripeY += laneStripeH * 2.5f
                    }
                    
                    // Render selected car
                    drawPixelSprite(
                        sprite = activeCar.spriteRaw,
                        centerX = size.width / 2f,
                        centerY = size.height / 2f,
                        pixelSize = 5.5f
                    )
                }
                
                // "TOP-DOWN VIEW" tag in bottom right
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(14.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(50.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = Localization.loc("top_down_engine", gameSave.language),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Side-by-side Bento Row of Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // High Score Bento Block (Light theme)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(110.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(GamingColors.BentoLightCard)
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                        .padding(14.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = Localization.loc("high_score", gameSave.language).uppercase(),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = GamingColors.BentoTextDark.copy(alpha = 0.7f),
                            fontSize = 10.sp
                        )
                        Text(
                            text = String.format("%06d", gameSave.highScore),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = GamingColors.BentoTextDark,
                            fontSize = 20.sp,
                            modifier = Modifier.testTag("high_score_value")
                        )
                        // decorative bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(50.dp))
                                .background(GamingColors.BentoTextDark.copy(alpha = 0.15f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(0.85f)
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(GamingColors.BentoTextDark)
                            )
                        }
                    }
                }

                // Current Drive Bento Block (Dark theme)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(110.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(GamingColors.BentoDarkCard)
                        .border(1.dp, GamingColors.BentoBorder, RoundedCornerShape(24.dp))
                        .padding(14.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = Localization.loc("current_drive", gameSave.language).uppercase(),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = GamingColors.BentoTextLight,
                            fontSize = 10.sp
                        )
                        Text(
                            text = activeCar.name.uppercase(),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = activeCar.colorAccent,
                            fontSize = 14.sp,
                            maxLines = 1
                        )
                        // level dots or indicator lines
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(50.dp)).background(activeCar.colorAccent))
                            Box(modifier = Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(50.dp)).background(activeCar.colorAccent))
                            Box(modifier = Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(50.dp)).background(GamingColors.BentoBorder))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))

            // THEME / STAGE SELECTOR CARD (tactile Bento layout)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(GamingColors.BentoDarkCard)
                    .border(1.dp, GamingColors.BentoBorder, RoundedCornerShape(24.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = Localization.loc("select_theme", gameSave.language),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 9.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left cycle button
                        IconButton(
                            onClick = { viewModel.cycleTheme(forward = false) },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(50.dp))
                                .background(Color.Black.copy(alpha = 0.4f))
                                .testTag("theme_btn_left")
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowLeft,
                                contentDescription = "Prev Theme",
                                tint = Color.White
                            )
                        }

                        // Central Theme Specs
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = viewModel.activeTheme.displayName.uppercase(),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                color = viewModel.activeTheme.curbColor1,
                                fontSize = 15.sp,
                                modifier = Modifier.testTag("active_theme_text")
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = Localization.loc("special_obstacles_active", gameSave.language),
                                fontFamily = FontFamily.Monospace,
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Right cycle button
                        IconButton(
                            onClick = { viewModel.cycleTheme(forward = true) },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(50.dp))
                                .background(Color.Black.copy(alpha = 0.4f))
                                .testTag("theme_btn_right")
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = "Next Theme",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            // START RETRO RACE Button (Bento 3D style)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.Black) // bottom shadow
            ) {
                Button(
                    onClick = { viewModel.navigateTo(GameState.PLAYING) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 4.dp) // creates thick tactile bottom
                        .testTag("button_start_game"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF)),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 24.dp, bottomEnd = 24.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Race",
                            tint = GamingColors.BentoTextDark,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = Localization.loc("start_race", gameSave.language),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = GamingColors.BentoTextDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // GARAGE AND UPGRADES Button (Dark Bento 3D style)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.Black)
            ) {
                Button(
                    onClick = { viewModel.navigateTo(GameState.SHOP) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 4.dp)
                        .testTag("button_open_garage"),
                    colors = ButtonDefaults.buttonColors(containerColor = GamingColors.BentoMediumCard),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = "Upgrades",
                            tint = GamingColors.CyberGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = Localization.loc("garage_upgrades", gameSave.language).uppercase(),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = GamingColors.CyberGold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // SETTINGS Button (Dark Bento 3D style)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.Black)
            ) {
                Button(
                    onClick = { viewModel.navigateTo(GameState.SETTINGS) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 4.dp)
                        .testTag("button_open_settings"),
                    colors = ButtonDefaults.buttonColors(containerColor = GamingColors.BentoMediumCard),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = Localization.loc("settings_btn", gameSave.language).uppercase(),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// --------------------- GARAGE & UPGRADES SHOP --------------------- //
@Composable
fun UpgradeShopLayout(viewModel: GameViewModel, gameSave: GameSave) {
    val pagerState = remember { mutableStateOf(gameSave.selectedCarId) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F))
            .bentoGridBackground()
    ) {
        // Upper Bento navigation block
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50.dp))
                    .background(GamingColors.BentoDarkCard)
                    .border(1.dp, GamingColors.BentoBorder, RoundedCornerShape(50.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(GameState.MENU) },
                    modifier = Modifier.testTag("shop_back_button").size(36.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Go back", tint = Color.White, modifier = Modifier.size(20.dp))
                }

                Text(
                    text = Localization.loc("garage_title", gameSave.language).uppercase(),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    fontSize = 14.sp
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(Color.Black.copy(alpha = 0.3f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Star, contentDescription = "Coins balance", tint = GamingColors.CyberGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${gameSave.coins}",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = GamingColors.CyberGold,
                        fontSize = 12.sp,
                        modifier = Modifier.testTag("shop_coins_balance")
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- SECTION 1: Bento Vehicle Selector ---
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(32.dp))
                        .background(GamingColors.BentoDarkCard)
                        .border(1.dp, GamingColors.BentoBorder, RoundedCornerShape(32.dp))
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "ACTIVE SPECIFICATION",
                            fontFamily = FontFamily.Monospace,
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        val maxVehicles = PixelSprites.UnlockedCarsList.size
                        val currentPreviewId = pagerState.value
                        val currentCarDef = PixelSprites.UnlockedCarsList[currentPreviewId]
                        val isUnlocked = gameSave.isCarUnlocked(currentPreviewId)
                        val isSelected = gameSave.selectedCarId == currentPreviewId

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    val prev = if (currentPreviewId - 1 < 0) maxVehicles - 1 else currentPreviewId - 1
                                    pagerState.value = prev
                                },
                                modifier = Modifier.testTag("car_prev_button")
                            ) {
                                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Previous car", tint = Color.White, modifier = Modifier.size(32.dp))
                            }

                            // Pixel Render car drawing canvas with custom Bento framing
                            Box(
                                modifier = Modifier
                                    .size(width = 150.dp, height = 110.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(GamingColors.ScreenCap)
                                    .border(1.dp, GamingColors.BentoBorder, RoundedCornerShape(24.dp))
                                    .bentoGridBackground(), // dotted background inside view
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    drawPixelSprite(
                                        sprite = currentCarDef.spriteRaw,
                                        centerX = size.width / 2f,
                                        centerY = size.height / 2f,
                                        pixelSize = 4.5f
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    val next = if (currentPreviewId + 1 >= maxVehicles) 0 else currentPreviewId + 1
                                    pagerState.value = next
                                },
                                modifier = Modifier.testTag("car_next_button")
                            ) {
                                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next car", tint = Color.White, modifier = Modifier.size(32.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Vehicle Name
                        Text(
                            text = currentCarDef.name.uppercase(),
                            fontFamily = FontFamily.Monospace,
                            color = currentCarDef.colorAccent,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Stats indicators in a clean stacked block
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CarStatMeter(label = Localization.loc("top_speed", gameSave.language), valueMultiplier = currentCarDef.baseSpeedMultiplier)
                            CarStatMeter(label = Localization.loc("handling", gameSave.language), valueMultiplier = currentCarDef.baseHandlingMultiplier)
                            CarStatMeter(label = Localization.loc("base_armor", gameSave.language), valueMultiplier = 1.0f + currentCarDef.extraShields * 0.5f)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Primary Action Button (Bento 3D style)
                        if (isUnlocked) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(Color.Black)
                            ) {
                                Button(
                                    onClick = { viewModel.selectVehicle(currentPreviewId) },
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(bottom = 3.dp)
                                        .testTag("car_use_button_${currentPreviewId}"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) Color(0xFFD0BCFF) else Color.White
                                    ),
                                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 21.dp, bottomEnd = 21.dp)
                                ) {
                                    Text(
                                        text = if (isSelected) Localization.loc("equipped", gameSave.language) else Localization.loc("equip", gameSave.language),
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp,
                                        color = if (isSelected) Color(0xFF21005D) else Color.Black
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(Color.Black)
                            ) {
                                Button(
                                    onClick = { viewModel.unlockVehicle(currentPreviewId, currentCarDef.cost) },
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(bottom = 3.dp)
                                        .testTag("car_unlock_button_${currentPreviewId}"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (gameSave.coins >= currentCarDef.cost) GamingColors.CyberGold else Color.Gray
                                    ),
                                    enabled = gameSave.coins >= currentCarDef.cost,
                                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 21.dp, bottomEnd = 21.dp)
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = Localization.loc("unlock_for", gameSave.language).replace("%s", currentCarDef.cost.toString()),
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- SECTION 2: 2x2 Bento Performance Grid ---
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = Localization.loc("performance_interpolators", gameSave.language).uppercase(),
                        fontFamily = FontFamily.Monospace,
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            BentoUpgradeCardLight(
                                name = "Engine Tuning",
                                currentLvl = gameSave.speedLevel,
                                cost = (gameSave.speedLevel + 1) * 40,
                                canAfford = gameSave.coins >= (gameSave.speedLevel + 1) * 40,
                                onUpgrade = { viewModel.purchaseUpgrade("SPEED") },
                                testTagPrefix = "speed",
                                lang = gameSave.language
                            )
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            BentoUpgradeCardDark(
                                name = "Chassis Armor",
                                currentLvl = gameSave.shieldLevel,
                                cost = (gameSave.shieldLevel + 1) * 50,
                                canAfford = gameSave.coins >= (gameSave.shieldLevel + 1) * 50,
                                onUpgrade = { viewModel.purchaseUpgrade("SHIELD") },
                                testTagPrefix = "shield",
                                lang = gameSave.language
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            BentoUpgradeCardMedium(
                                name = "Tire Traction",
                                currentLvl = gameSave.handlingLevel,
                                cost = (gameSave.handlingLevel + 1) * 35,
                                canAfford = gameSave.coins >= (gameSave.handlingLevel + 1) * 35,
                                onUpgrade = { viewModel.purchaseUpgrade("HANDLING") },
                                testTagPrefix = "handling",
                                lang = gameSave.language
                            )
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            BentoUpgradeCardDarkAlt(
                                name = "Coin Harvester",
                                currentLvl = gameSave.coinValueLevel,
                                cost = (gameSave.coinValueLevel + 1) * 45,
                                canAfford = gameSave.coins >= (gameSave.coinValueLevel + 1) * 45,
                                onUpgrade = { viewModel.purchaseUpgrade("COIN") },
                                testTagPrefix = "coin",
                                lang = gameSave.language
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            BentoUpgradeCardLight(
                                name = "Nitrogen Booster",
                                currentLvl = gameSave.accelerationLevel,
                                cost = (gameSave.accelerationLevel + 1) * 30,
                                canAfford = gameSave.coins >= (gameSave.accelerationLevel + 1) * 30,
                                onUpgrade = { viewModel.purchaseUpgrade("ACCELERATION") },
                                testTagPrefix = "acceleration",
                                lang = gameSave.language
                            )
                        }

                        // Decorative info box to complete the grid nicely
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(120.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(GamingColors.BentoDarkCard)
                                .border(1.dp, GamingColors.BentoBorder, RoundedCornerShape(24.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.15f),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = Localization.loc("fast_acceleration", gameSave.language),
                                    color = Color.White.copy(alpha = 0.3f),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CarStatMeter(label: String, valueMultiplier: Float) {
    val barProgress = (valueMultiplier - 0.9f).coerceIn(0.1f, 1.0f)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 11.sp,
            modifier = Modifier.width(85.dp)
        )
        LinearProgressIndicator(
            progress = { barProgress },
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = GamingColors.NeonBlue,
            trackColor = Color.Gray.copy(alpha = 0.2f),
        )
    }
}

@Composable
private fun UpgradeRow(
    name: String,
    description: String,
    currentLvl: Int,
    cost: Int,
    coinsAva: Int,
    onUpgrade: () -> Unit,
    testTagPrefix: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "$name (LVL $currentLvl/5)",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 13.sp
            )
            Text(
                text = description,
                fontFamily = FontFamily.Monospace,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 10.sp,
                lineHeight = 13.sp,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))
            
            // Level indicator bars
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                for (i in 1..5) {
                    Box(
                        modifier = Modifier
                            .size(width = 15.dp, height = 4.dp)
                            .background(
                                if (i <= currentLvl) GamingColors.LedText else Color.Gray.copy(alpha = 0.2f)
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (currentLvl >= 5) {
            Box(
                modifier = Modifier
                    .border(1.dp, GamingColors.SteelBorder, RoundedCornerShape(4.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "MAX",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = GamingColors.LedText,
                    fontSize = 11.sp
                )
            }
        } else {
            val canAfford = coinsAva >= cost
            Button(
                onClick = onUpgrade,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canAfford) GamingColors.Crimson else Color.Gray.copy(alpha = 0.3f),
                    disabledContainerColor = Color.Gray.copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(4.dp),
                enabled = canAfford,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier
                    .height(36.dp)
                    .testTag("btn_upgrade_${testTagPrefix}")
            ) {
                Icon(Icons.Default.Star, contentDescription = null, tint = if (canAfford) GamingColors.CyberGold else Color.Gray, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$cost",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (canAfford) Color.White else Color.Gray
                )
            }
        }
    }
}


// --------------------- CRASHED / GAME OVER OVERLAY --------------------- //
@Composable
fun GameOverLayout(viewModel: GameViewModel, gameSave: GameSave) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F))
            .bentoGridBackground() // Blueprint dot grid background Overlay
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            // Dynamic blinking crash alert frame
            val blinkAction = rememberInfiniteTransition("crash_pulse")
            val isHotRed by blinkAction.animateColor(
                initialValue = GamingColors.Crimson,
                targetValue = Color.Red.copy(alpha = 0.2f),
                animationSpec = infiniteRepeatable(tween(250, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
                label = "pulse"
            )

            // Bento Blinking Warning Banner Block
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(GamingColors.BentoDarkCard)
                    .border(2.dp, isHotRed, RoundedCornerShape(32.dp))
                    .padding(vertical = 24.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = Localization.loc("system_error", gameSave.language).uppercase(),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = GamingColors.Crimson.copy(alpha = 0.8f),
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = Localization.loc("vehicle_crashed", gameSave.language).uppercase(),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = GamingColors.Crimson,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("crash_label")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bento Scoreboard Statistics Block
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(GamingColors.BentoMediumCard)
                    .border(1.dp, GamingColors.BentoBorder, RoundedCornerShape(32.dp))
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = Localization.loc("run_score_stats", gameSave.language).uppercase(),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = GamingColors.BentoTextLight.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Score value card section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Localization.loc("your_run_score", gameSave.language).uppercase(),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            color = GamingColors.BentoTextLight,
                            fontSize = 12.sp
                        )
                        Text(
                            text = String.format("%06d", viewModel.currentScore),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = GamingColors.ActiveGreen,
                            fontSize = 24.sp,
                            modifier = Modifier.testTag("final_score")
                        )
                    }

                    // Check If is high score breakthrough info tag
                    if (viewModel.currentScore >= gameSave.highScore) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(GamingColors.Crimson)
                                .padding(horizontal = 14.dp, vertical = 4.dp)
                                .testTag("new_record_tag")
                        ) {
                            Text(
                                text = Localization.loc("new_personal_record", gameSave.language).uppercase(),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 9.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = GamingColors.BentoBorder)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Collected coins metric inside Bento statistics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Localization.loc("coins_salvaged", gameSave.language).uppercase(),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            color = GamingColors.BentoTextLight,
                            fontSize = 12.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = GamingColors.CyberGold, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+${viewModel.coinsCollectedThisRun}",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                color = GamingColors.CyberGold,
                                fontSize = 20.sp,
                                modifier = Modifier.testTag("salvaged_coins")
                            )
                        }
                    }
                }
            }
        }

        // Action Buttons with modern heavy Bento 3D tactile deck spacing
        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            // RESTART Buttons (Large accented 3D action styling)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.Black)
            ) {
                Button(
                    onClick = { viewModel.navigateTo(GameState.PLAYING) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 4.dp)
                        .testTag("btn_save_restart"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF)),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Refresh, contentDescription = "Restart", tint = Color.Black, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = Localization.loc("restart_drive", gameSave.language).uppercase(),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // CHOOSE TO GO TO MAIN MENU Action Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.Black)
            ) {
                Button(
                    onClick = { viewModel.navigateTo(GameState.MENU) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 4.dp)
                        .testTag("btn_save_menu"),
                    colors = ButtonDefaults.buttonColors(containerColor = GamingColors.BentoMediumCard),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Home, contentDescription = "Home", tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = Localization.loc("drive_to_main_menu", gameSave.language).uppercase(),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StageClearLayout(viewModel: GameViewModel, gameSave: GameSave) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F))
            .bentoGridBackground()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.height(20.dp))

            // Upper Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = Localization.loc("stage_clear_sub", gameSave.language).uppercase(),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = GamingColors.ActiveGreen,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = Localization.loc("stage_cleared", gameSave.language).uppercase(),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic,
                        color = Color.White,
                        letterSpacing = (-1).sp,
                        modifier = Modifier.testTag("stage_clear_title")
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Large center Bento Board of score details
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(GamingColors.BentoDarkCard)
                    .border(1.dp, GamingColors.BentoBorder, RoundedCornerShape(32.dp))
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Status
                    Text(
                        text = Localization.loc("drive_analysis", gameSave.language).replace("%s", viewModel.activeTheme.displayName.uppercase()).uppercase(),
                        fontFamily = FontFamily.Monospace,
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )

                    HorizontalDivider(color = GamingColors.BentoBorder, thickness = 1.dp)

                    // Final Total Score
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Localization.loc("total_run_score", gameSave.language).uppercase(),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Text(
                            text = String.format("%06d", viewModel.currentScore),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = GamingColors.NeonBlue,
                            fontSize = 24.sp,
                            modifier = Modifier.testTag("clear_total_score")
                        )
                    }

                    HorizontalDivider(color = GamingColors.BentoBorder, thickness = 1.dp)

                    // Statistics rows
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = Localization.loc("time_taken", gameSave.language),
                            fontFamily = FontFamily.Monospace,
                            color = GamingColors.BentoTextLight.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                        Text(
                            text = String.format("%.1f %s", viewModel.gameTimeSec, Localization.loc("seconds", gameSave.language).uppercase()),
                            fontFamily = FontFamily.Monospace,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = Localization.loc("completion_bonus", gameSave.language),
                            fontFamily = FontFamily.Monospace,
                            color = GamingColors.BentoTextLight.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                        val timeBonus = ((60f - viewModel.gameTimeSec).coerceAtLeast(0f) * 150f).toInt()
                        Text(
                            text = "+$timeBonus pts",
                            fontFamily = FontFamily.Monospace,
                            color = GamingColors.ActiveGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = Localization.loc("near_miss_passes", gameSave.language),
                            fontFamily = FontFamily.Monospace,
                            color = GamingColors.BentoTextLight.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                        val textSuffix = if (gameSave.language == "it") "volte" else "times"
                        Text(
                            text = "${viewModel.nearMissesCount} $textSuffix",
                            fontFamily = FontFamily.Monospace,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.testTag("clear_near_misses")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Localization.loc("coins_acquired", gameSave.language).uppercase(),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            color = GamingColors.BentoTextLight,
                            fontSize = 12.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = GamingColors.CyberGold, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+${viewModel.coinsCollectedThisRun}",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                color = GamingColors.CyberGold,
                                fontSize = 20.sp,
                                modifier = Modifier.testTag("completed_run_coins")
                            )
                        }
                    }
                }
            }
        }

        // Action buttons (Next drive vs Menu)
        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            // PLAY NEXT STAGE Button (tactile 3D)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.Black)
            ) {
                Button(
                    onClick = { viewModel.startNewGameRun() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 4.dp)
                        .testTag("btn_clear_next"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF)),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Next Drive", tint = Color.Black, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = Localization.loc("drive_again", gameSave.language).uppercase(),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // HOME MENU Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.Black)
            ) {
                Button(
                    onClick = { viewModel.navigateTo(GameState.MENU) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 4.dp)
                        .testTag("btn_clear_menu"),
                    colors = ButtonDefaults.buttonColors(containerColor = GamingColors.BentoMediumCard),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Home, contentDescription = "Home", tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = Localization.loc("drive_to_main_menu", gameSave.language).uppercase(),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsLayout(viewModel: GameViewModel, gameSave: GameSave) {
    val lang = gameSave.language
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F))
            .bentoGridBackground()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.height(20.dp))

            // Upper Back Navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50.dp))
                    .background(GamingColors.BentoDarkCard)
                    .border(1.dp, GamingColors.BentoBorder, RoundedCornerShape(50.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(GameState.MENU) },
                    modifier = Modifier.testTag("settings_back_button").size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Go back",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = Localization.loc("settings_title", lang),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.width(36.dp)) // Equal space
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Large center Bento Board of Language selectors
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(GamingColors.BentoDarkCard)
                    .border(1.dp, GamingColors.BentoBorder, RoundedCornerShape(32.dp))
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Status
                    Text(
                        text = Localization.loc("select_language", lang).uppercase(),
                        fontFamily = FontFamily.Monospace,
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    HorizontalDivider(color = GamingColors.BentoBorder, thickness = 1.dp)

                    // English Select Indicator
                    val isEnglishSelected = lang == "en"
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isEnglishSelected) GamingColors.BentoMediumCard else Color.Transparent)
                            .border(
                                width = 1.dp,
                                color = if (isEnglishSelected) Color(0xFFD0BCFF) else GamingColors.BentoBorder,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { viewModel.selectLanguage("en") }
                            .padding(16.dp)
                            .testTag("language_option_en")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ENGLISH / EN",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (isEnglishSelected) Color(0xFFD0BCFF) else Color.White,
                                fontSize = 14.sp
                            )
                            if (isEnglishSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color(0xFFD0BCFF),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Italian Select Indicator
                    val isItalianSelected = lang == "it"
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isItalianSelected) GamingColors.BentoMediumCard else Color.Transparent)
                            .border(
                                width = 1.dp,
                                color = if (isItalianSelected) Color(0xFFD0BCFF) else GamingColors.BentoBorder,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { viewModel.selectLanguage("it") }
                            .padding(16.dp)
                            .testTag("language_option_it")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ITALIANO / IT",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (isItalianSelected) Color(0xFFD0BCFF) else Color.White,
                                fontSize = 14.sp
                            )
                            if (isItalianSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color(0xFFD0BCFF),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = GamingColors.BentoBorder, thickness = 1.dp)

                    // Current status row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Localization.loc("active_configuration", lang).uppercase(),
                            fontFamily = FontFamily.Monospace,
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp
                        )
                        Text(
                            text = if (isItalianSelected) "ITALIANO" else "ENGLISH",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD0BCFF),
                            fontSize = 12.sp,
                            modifier = Modifier.testTag("active_language_tag")
                        )
                    }
                }
            }
        }

        // Action Menu Buttons at Bottom
        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            // BACK TO MENU Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.Black)
            ) {
                Button(
                    onClick = { viewModel.navigateTo(GameState.MENU) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 4.dp)
                        .testTag("btn_settings_back_to_menu"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF)),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home",
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = Localization.loc("back_to_menu", lang).uppercase(),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}
