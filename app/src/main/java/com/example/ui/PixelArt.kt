package com.example.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform

object PixelColors {
    val Transparent = Color.Transparent
    val Black = Color(0xFF18181A)
    val White = Color(0xFFF0F0FA)
    val Red = Color(0xFFD62828)
    val DarkRed = Color(0xFF9E1B1B)
    val Yellow = Color(0xFFF77F00)
    val Gold = Color(0xFFFCBF49)
    val Orange = Color(0xFFFF5400)
    val Cyan = Color(0xFF00E5FF)
    val DarkCyan = Color(0xFF00B0FF)
    val Blue = Color(0xFF003049)
    val LightBlue = Color(0xFF4EA8DE)
    val Grey = Color(0xFF6C757D)
    val LightGrey = Color(0xFFADB5BD)
    val DarkGrey = Color(0xFF343A40)
    val Green = Color(0xFF38B000)
    val DarkGreen = Color(0xFF007200)
    val Brown = Color(0xFF7F5539)
    val WoodLight = Color(0xFFB5179E) // Purple theme accent / Retro wood
    val Skin = Color(0xFFFFC6FF)
}

data class CarDef(
    val id: Int,
    val name: String,
    val cost: Int,
    val baseSpeedMultiplier: Float,  // Speed modifier
    val baseHandlingMultiplier: Float,// Shift speed modifier
    val extraShields: Int,            // Starting additional life points
    val spriteRaw: List<String>,
    val colorAccent: Color
)

object PixelSprites {
    // Character mappings
    val DefaultColorMap = mapOf(
        '.' to PixelColors.Transparent,
        'k' to PixelColors.Black,
        'w' to PixelColors.White,
        'r' to PixelColors.Red,
        'd' to PixelColors.DarkRed,
        'y' to PixelColors.Yellow,
        'g' to PixelColors.Grey,
        'l' to PixelColors.LightGrey,
        'x' to PixelColors.DarkGrey,
        'o' to PixelColors.Orange,
        'c' to PixelColors.Cyan,
        'b' to PixelColors.Blue,
        'u' to PixelColors.LightBlue,
        'e' to PixelColors.Green,
        'n' to PixelColors.DarkGreen,
        's' to PixelColors.Gold,
        't' to PixelColors.Brown
    )

    // Player Car 0: Classic Red Racer (Sleek sports car)
    val PlayerClassicRed = listOf(
        "....kkkk....",
        "...krrrrk...",
        "..krrrrrrk..",
        ".krwwrrwwrk.",
        ".krrrrrrrrk.",
        ".kkkrrrrkkk.",
        "kxxkbbbbkxxk",
        "krxkbbbbkxrk",
        "krrkbbbbkrrk",
        "krrrrrrrrrrk",
        ".kkkrrwwkkk.",
        ".krrrrrrrrk.",
        ".krrkkkkrrk.",
        "..kk....kk.."
    )

    // Player Car 1: Cyber Neon Grid (Neon cyan, high-tech tires)
    val PlayerCyberGrid = listOf(
        "....kkkk....",
        "...kcccck...",
        "..kccccuck..",
        ".kcuccccuck.",
        ".kcccccccck.",
        ".kkkcccckkk.",
        "kyykbbbbkyyk",
        "kcykbbbbkyck",
        "kcckbbbbkcck",
        "kcccccccccck",
        ".kkkccuukkk.",
        ".kcccccccck.",
        ".kcckkkkcck.",
        "..kk....kk.."
    )

    // Player Car 2: Gold SuperVeloce (Retro muscle / gold racer)
    val PlayerSovereignGold = listOf(
        "....kkkk....",
        "...ksssk...",
        "..kssssssk..",
        ".kswsssswsk.",
        ".kssssssssk.",
        ".kkksssskkk.",
        "kxxkbbbbkxxk",
        "ksxkbbbbkxsk",
        "ksskbbbbkssk",
        "kssssssssssk",
        ".kkksswwkkk.",
        ".kssssssssk.",
        ".ksskkkkssk.",
        "..kk....kk.."
    )

    // Player Car 3: Hyperion (Sleek black ship/car with green trails)
    val PlayerHyperion = listOf(
        "....kkkk....",
        "...keeek...",
        "..keeeeeek..",
        ".keweeeewek.",
        ".keeeeeeeek.",
        ".kkkeeeekkk.",
        "kyykbbbbkyyk",
        "keykbbbbkyek",
        "keekbbbbkeek",
        "keeeeeeeeeek",
        ".kkkeewwkkk.",
        ".keeeeeeeek.",
        ".keekkkkkeek.",
        "..kk....kk.."
    )

    // Obstacle Car 1: Standard Blue Commuter
    val ObstacleBlueCar = listOf(
        "....kkkk....",
        "...kbbbbk...",
        "..kbbwbbbk..",
        ".kbbwwwbbbk.",
        "kxxkbbbbkxxk",
        "kbxkllllkxbk",
        "kbbkllllkbbk",
        "kbbkllllkbbk",
        "kbbbbbbbbbbk",
        ".kkkbbwwkkk.",
        ".kbbbbbbbbk.",
        ".kbbkkkkbbk.",
        "..kk....kk.."
    )

    // Obstacle Car 2: Speeding Orange Convertible
    val ObstacleOrangeCar = listOf(
        "....kkkk....",
        "...koook...",
        "..koooooook..",
        ".kowoooowok.",
        "kxxkoooookxxk",
        "koxkllllkxok",
        "kookllllkook",
        "kookllllkook",
        "kooooooooook",
        ".kkkoowwkkk.",
        ".kooooooook.",
        ".kookkkkook.",
        "..kk....kk.."
    )

    // Truck Obstacle: 12x24 grid (huge block, spans larger area)
    val ObstacleTruck = listOf(
        "...kkkkkk...",
        "..kggggggk..",
        ".kglgggggLk.",
        ".kggggggggk.",
        "kkkggggggkkk",
        "kxkbbbbbbkxk",
        "kgkbbbbbbkgk",
        "kgkkkkkkkkgk",
        "kgkggggggkgk",
        "kgkggggggkgk",
        "kgkggggggkgk",
        "kgkggggggkgk",
        "kgkggggggkgk",
        "kgkggggggkgk",
        "kkkggggggkkk",
        "kxkggggggkxk",
        "kgkggggggkgk",
        "kgkggggggkgk",
        "kgkggggggkgk",
        "kgkggggggkgk",
        "kgkggggggkgk",
        "kkkkkkkkkkkk",
        ".kxk....kxk.",
        "..k......k.."
    )

    // Roadblock Barrier (Construction sawhorse)
    val ObstacleRoadblock = listOf(
        "kkkkkkkkkkkkkkkk",
        "kooooyyyyooooyyy",
        "kyyyyooooyyyyooo",
        "kkkkkkkkkkkkkkkk",
        "kttkkkkkkkkkkttk",
        "ktk..........ktk",
        "ktk..........ktk",
        "k............k.."
    )

    // Oil Spill (Slick black/dark grey puddle, wider and organic)
    val ObstacleOilSpill = listOf(
        "......kkkk......",
        "....kkxxxxkk....",
        "..kkxxxxxxxxkk..",
        ".kxxxxxxxxxxxxk.",
        "kxxxxxxxxxxxxxxk",
        "kxxxxxxxxxxxxxxk",
        ".kxxxxxxxxxxxxk.",
        "..kkxxxxxxxxkk..",
        "....kkxxxxkk....",
        "......kkkk......"
    )

    // Tumbleweed obstacle (dry brown circle)
    val ObstacleTumbleweed = listOf(
        "....tttt....",
        "..ttxxxxtext..",
        ".ttxxxtttxxett.",
        "ttxxxtxttxxxtt",
        "txxxtxtttxxxtt",
        "ttxxxttttxxxet",
        ".ttxxxxxxxett.",
        "..ttxxxxtext..",
        "....tttt...."
    )

    // Sand trap (dry desert sand pit)
    val ObstacleSandTrap = listOf(
        "....ssss....",
        "..ssssssss..",
        ".sswwsssswws.",
        "sswwsssssswws",
        "sssssssssssss",
        "sssssssssssss",
        ".ssssssssssws.",
        "..ssssssss..",
        "....ssss...."
    )

    // Ice patch (shiny ice panel)
    val ObstacleIcePatch = listOf(
        "....cccc....",
        "..ccuuuucc..",
        ".ccuuuuuuucc.",
        "ccuuuuuuuuucc",
        "cuuuuuuuuuuuc",
        "cuuuuuuuuuuuc",
        ".ccuuuuuuucc.",
        "..ccuuuucc..",
        "....cccc...."
    )

    // Snow drift (glowing white pile)
    val ObstacleSnowDrift = listOf(
        "......wwww......",
        "....wwuuuuww....",
        "..wwuuuuuuuuww..",
        ".wwuuuuuuuuuuww.",
        "wwuuuuuuuuuuuuww",
        "wwuuuuuuuuuuuuww",
        ".wwuuuuuuuuuuww.",
        "..wwuuuuuuuuww..",
        "....wwuuuuww....",
        "......wwww......"
    )

    // Spike strip (metal strip with pointy spikes)
    val ObstacleSpikeStrip = listOf(
        "kkkkkkkkkkkkkkkk",
        "kyyykyyykyyykyyy",
        "kkkkkkkkkkkkkkkk",
        "kxxkkxxkkxxkkxxk"
    )

    // Traffic cone (orange and white)
    val ObstacleTrafficCone = listOf(
        "......oo......",
        ".....oooo.....",
        "....oowwoo....",
        "....oowwoo....",
        "...ooxxxooo...",
        "..ooxxxxxooo..",
        ".oooooooooooo.",
        "kkkkkkkkkkkkkk"
    )

    // Coin Sprite
    val CoinStar = listOf(
        "...kkkk...",
        "..kssssk..",
        ".ksswwssk.",
        "ksswwssswk",
        "ksswsssswk",
        "kssssssssk",
        ".kssssssk.",
        "...kkkk..."
    )

    val UnlockedCarsList = listOf(
        CarDef(0, "Red Bandit", 0, 1.0f, 1.0f, 0, PlayerClassicRed, PixelColors.Red),
        CarDef(1, "Neon Cyber", 120, 1.2f, 1.3f, 1, PlayerCyberGrid, PixelColors.Cyan),
        CarDef(2, "Apex Aura", 380, 1.4f, 1.5f, 1, PlayerSovereignGold, PixelColors.Gold),
        CarDef(3, "Cosmic Viper", 850, 1.6f, 1.7f, 2, PlayerHyperion, PixelColors.Green)
    )
}

// Draw a procedurally loaded pixel array onto the Compose Canvas.
fun DrawScope.drawPixelSprite(
    sprite: List<String>,
    centerX: Float,
    centerY: Float,
    pixelSize: Float,
    colorMap: Map<Char, Color> = PixelSprites.DefaultColorMap,
    rotation: Float = 0f,
    flashTransparent: Boolean = false
) {
    if (flashTransparent) return // Don't draw if flashing invisible
    
    val rows = sprite.size
    val cols = sprite[0].length
    
    val width = cols * pixelSize
    val height = rows * pixelSize
    
    val startX = centerX - width / 2f
    val startY = centerY - height / 2f

    withTransform({
        if (rotation != 0f) {
            rotate(rotation, Offset(centerX, centerY))
        }
    }) {
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val char = sprite[r][c]
                val color = colorMap[char] ?: Color.Transparent
                if (color != Color.Transparent) {
                    drawRect(
                        color = color,
                        topLeft = Offset(startX + c * pixelSize, startY + r * pixelSize),
                        size = Size(pixelSize, pixelSize)
                    )
                }
            }
        }
    }
}
