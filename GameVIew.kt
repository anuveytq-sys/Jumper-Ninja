package com.example.benny

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.view.View
import android.content.SharedPreferences
import android.view.MotionEvent
import android.graphics.Rect
import android.graphics.Paint
import android.graphics.Color
import android.media.MediaPlayer

class GameView(context: Context) : View(context) {

    private val musicPlayer = MediaPlayer.create(
        context,
        R.raw.background_music
    ).apply {
        isLooping = true
    }
    private val screenWidth = resources.displayMetrics.widthPixels
    private val screenHeight = resources.displayMetrics.heightPixels

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    // Background
    private val background = Bitmap.createScaledBitmap(
        BitmapFactory.decodeResource(resources, R.drawable.background),
        screenWidth,
        screenHeight,
        true
    )
    private val startBackground = Bitmap.createScaledBitmap(
        BitmapFactory.decodeResource(resources, R.drawable.start_background),
        screenWidth,
        screenHeight,
        true
    )

    // Player
    private val player = Bitmap.createScaledBitmap(
        BitmapFactory.decodeResource(resources, R.drawable.player),
        250,
        250,
        true
    )
    private val obstacle = Bitmap.createScaledBitmap(
        BitmapFactory.decodeResource(resources, R.drawable.obstacle),
        120,
        120,
        true
    )
    private val shield = Bitmap.createScaledBitmap(
    BitmapFactory.decodeResource(resources, R.drawable.shield),
    80,
    80,
    true
    )

    private var shieldX = 0f
    private var shieldY = 0f
    private val shieldRect = Rect()
    private var hasShield = false

    // Shield icon position on the top-right
    private val shieldIconX = screenWidth - 120f
    private val shieldIconY = 80f
    private val prefs: SharedPreferences =
        context.getSharedPreferences("game_data", Context.MODE_PRIVATE)

    private var bestScore = prefs.getInt("best_score", 0)

    private val retryPaint = Paint().apply {
        color = Color.WHITE
        textSize = 70f
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
    }

    private val retryRect = android.graphics.RectF()

    private val homeRect = android.graphics.RectF()

    private var obstacleX = (screenWidth + 1200).toFloat()
    private var obstacleY = 0f

    private var obstacleSpeed = 11f
    private val playerRect = Rect()
    private val obstacleRect = Rect()
    private var obstacleCount = 0
    private val coin = Bitmap.createScaledBitmap(
        BitmapFactory.decodeResource(resources, R.drawable.coin),
        80,
        80,
        true
    )

    private var coinX = -1000f
    private var coinY = -1000f
    private var coinActive = false
    private val coinRect = Rect()

    private var gameOver = false
    private var gameStarted = false
    private val level1ButtonRect = android.graphics.RectF()

    private val level2ButtonRect = android.graphics.RectF()

    private val shurikenButtonRect = android.graphics.RectF()
    private var score = 0

    private var currentLevel = 1

    // Level 2 - Drone
    private val drone = Bitmap.createScaledBitmap(
        BitmapFactory.decodeResource(resources, R.drawable.drone),
        150,
        100,
        true
    )

    private var droneX = 0f
    private var droneY = 0f
    private var droneSpeed = 12f
    private var droneActive = false

    private fun updateDroneSpeed() {
        droneSpeed = minOf(
            40f,
            12f + (score / 5f) * 1.5f
        )
    }

    // Level 2 - Shuriken
    private val shuriken = Bitmap.createScaledBitmap(
        BitmapFactory.decodeResource(resources, R.drawable.shuriken),
        70,
        70,
        true
    )

    private var shurikenX = 0f
    private var shurikenY = 0f
    private var shurikenActive = false

    private var playerX = 100f
    private val groundY = (screenHeight - 550).toFloat()

    private var playerY = groundY

    init {
        obstacleY = groundY + player.height - obstacle.height
    }

    private var velocity = 0f
    private val gravity = 2.0f
    private var isJumping = false

    private var bgX1 = 0f
    private var bgX2 = screenWidth.toFloat()

    private val bgSpeed = 20f

    private val topScorePaint = Paint().apply {
        color = Color.WHITE
        textSize = 70f
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
    }
    private val scoreBorderPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }
    private val overlayPaint = Paint().apply {
        color = Color.argb(170, 0, 0, 0)
    }

    private val gameOverPaint = Paint().apply {
        color = Color.WHITE
        textSize = 110f
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
    }
    private val tooBadPaint = Paint().apply {
        color = Color.WHITE
        textSize = 65f
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
    }
    private val gameOverScorePaint = Paint().apply {
        color = Color.WHITE
        textSize = 70f
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
    }
    private val gameOverBoxPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        if (event.action == MotionEvent.ACTION_DOWN) {
            performClick()

            if (!gameStarted) {

                // LEVEL 1 BUTTON
                if (level1ButtonRect.contains(event.x, event.y)) {

                    currentLevel = 1
                    gameStarted = true

                    score = 0
                    coinActive = false
                    coinX = -1000f
                    coinY = -1000f
                    obstacleSpeed = 11f
                    gameOver = false
                    hasShield = false
                    obstacleCount = 0

                    shieldX = -1000f
                    shieldY = obstacleY

                    playerX = 100f
                    playerY = groundY

                    velocity = 0f
                    isJumping = false

                    obstacleX = (screenWidth + 350).toFloat()

                    droneX = screenWidth + 500f
                    droneY = groundY - 350f
                    droneSpeed = 12f
                    droneActive = true

                    shurikenActive = false
                    shurikenX = -1000f
                    shurikenY = -1000f

                    if (!musicPlayer.isPlaying) {
                        musicPlayer.start()
                    }

                    invalidate()
                }

                // LEVEL 2 BUTTON
                else if (level2ButtonRect.contains(event.x, event.y)) {

                    currentLevel = 2
                    gameStarted = true

                    score = 0
                    coinActive = false
                    coinX = -1000f
                    coinY = -1000f
                    obstacleSpeed = 11f
                    gameOver = false
                    hasShield = false
                    obstacleCount = 0

                    shieldX = -1000f
                    shieldY = obstacleY

                    playerX = 100f
                    playerY = groundY

                    velocity = 0f
                    isJumping = false

                    obstacleX = (screenWidth + 350).toFloat()

                    droneX = screenWidth + 500f
                    droneY = groundY - 350f
                    droneSpeed = 12f
                    droneActive = true

                    shurikenActive = false
                    shurikenX = -1000f
                    shurikenY = -1000f

                    if (!musicPlayer.isPlaying) {
                        musicPlayer.start()
                    }

                    invalidate()
                }

                return true
            }

            if (gameOver) {

                if (retryRect.contains(event.x, event.y)) {

                    gameOver = false

                    score = 0
                    obstacleSpeed = 11f
                    droneSpeed = 12f
                    coinActive = false
                    coinX = -1000f
                    coinY = -1000f
                    droneActive = currentLevel == 2

                    droneX = screenWidth + 500f
                    droneY = groundY - 350f

                    shurikenActive = false
                    shurikenX = -1000f
                    shurikenY = -1000f

                    if (!musicPlayer.isPlaying) {
                        musicPlayer.start()
                    }

                    hasShield = false
                    obstacleCount = 0
                    shieldX = -1000f
                    shieldY = obstacleY

                    playerX = 100f
                    playerY = groundY

                    velocity = 0f
                    isJumping = false

                    obstacleX = (screenWidth + 350).toFloat()

                    invalidate()
                }
                if (gameOver && homeRect.contains(event.x, event.y)) {

                    gameOver = false
                    gameStarted = false

                    score = 0
                    obstacleCount = 0
                    obstacleSpeed = 11f

                    droneSpeed = 12f
                    droneActive = false

                    shurikenActive = false
                    shurikenX = -1000f
                    shurikenY = -1000f

                    coinActive = false
                    coinX = -1000f
                    coinY = -1000f

                    hasShield = false
                    shieldX = -1000f
                    shieldY = -1000f

                    playerX = 100f
                    playerY = groundY
                    velocity = 0f
                    isJumping = false

                    obstacleX = (screenWidth + 1200).toFloat()

                    musicPlayer.start()

                    invalidate()
                    performClick()
                    return true
                }

                return true
            }

            // LEVEL 2 - SHURIKEN BUTTON
            if (currentLevel == 2 && shurikenButtonRect.contains(event.x, event.y)) {

                if (!shurikenActive) {
                    shurikenX = playerX + player.width
                    shurikenY = playerY + player.height / 2f
                    shurikenActive = true
                }

                return true
            }

            if (!isJumping) {
                velocity = -42f
                isJumping = true
            }
        }

        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!gameOver && gameStarted) {

            if (obstacleY == 0f) {
                obstacleY = groundY + player.height - obstacle.height
            }

            // Move background
            bgX1 -= bgSpeed
            bgX2 -= bgSpeed

            if (bgX1 <= -screenWidth)
                bgX1 = bgX2 + screenWidth

            if (bgX2 <= -screenWidth)
                bgX2 = bgX1 + screenWidth

            // Move obstacle
            obstacleX -= obstacleSpeed

            if (obstacleX < -obstacle.width) {

                obstacleX = (screenWidth + 500).toFloat()
                obstacleY = groundY + player.height - obstacle.height

                score++
                obstacleCount++

                if (obstacleCount % 6 == 0) {
                    coinActive = true
                    coinX = obstacleX - obstacle.width - 100f
                    coinY = obstacleY
                }

                if (score % 5 == 0 && obstacleSpeed < 40f) {
                    obstacleSpeed += 2f
                }

                updateDroneSpeed()

                if (obstacleCount % 8 == 0) {

                    shieldX = obstacleX - shield.width - 30f
                    shieldY = obstacleY

                } else {

                    shieldX = -1000f

                }
            }
            if (obstacleCount % 8 == 0) {
                shieldX = obstacleX + obstacle.width + 20f
                shieldY = obstacleY
            } else {
                shieldX = -1000f
            }


            // Gravity
            if (isJumping) {
                velocity += gravity
                playerY += velocity

                if (playerY > groundY) {
                    playerY = groundY
                    velocity = 0f
                    isJumping = false
                }
            }
            // LEVEL 2 - MOVE DRONE
            if (currentLevel == 2 && droneActive) {

                droneX -= droneSpeed

                if (droneX < -drone.width) {
                    droneX = screenWidth + 500f
                    droneY = groundY - 350f

                    // Make sure the current score controls the speed
                    updateDroneSpeed()
                }
            }
            if (currentLevel == 2 && shurikenActive) {

                shurikenX += 30f

                if (shurikenX > screenWidth) {
                    shurikenActive = false
                    shurikenX = -1000f
                    shurikenY = -1000f
                }
            }
            // Move coin
            if (coinActive) {
                coinX -= obstacleSpeed

                if (coinX < -coin.width) {
                    coinActive = false
                    coinX = -1000f
                    coinY = -1000f
                }
            }
        }

        // Draw background
        canvas.drawBitmap(background, bgX1, 0f, null)
        canvas.drawBitmap(background, bgX2, 0f, null)

        val scoreBox = android.graphics.RectF(
            30f,
            80f,
            360f,
            180f
        )

        canvas.drawRoundRect(
            scoreBox,
            15f,
            15f,
            scoreBorderPaint
        )

        canvas.drawText(
            "Score : $score",
            scoreBox.centerX(),
            150f,
            topScorePaint
        )
        // Draw player
        canvas.drawBitmap(player, playerX, playerY, null)

        if (hasShield) {
            // Show shield icon at the top-right
            canvas.drawBitmap(
                shield,
                shieldIconX,
                shieldIconY,
                null
            )
        } else {
            // Show the collectible shield in the game
            canvas.drawBitmap(
                shield,
                shieldX,
                shieldY,
                null
            )
        }
        // Draw obstacle
        canvas.drawBitmap(obstacle, obstacleX, obstacleY, null)

        if (coinActive) {
            canvas.drawBitmap(
                coin,
                coinX,
                coinY,
                null
            )
        }

        // Draw Level 2 drone
        if (currentLevel == 2 && droneActive) {
            canvas.drawBitmap(
                drone,
                droneX,
                droneY,
                null
            )
        }

// Draw Level 2 shuriken
        if (currentLevel == 2 && shurikenActive) {
            canvas.drawBitmap(
                shuriken,
                shurikenX,
                shurikenY,
                null
            )
        }

        // Collision
        playerRect.set(
            (playerX + 40).toInt(),
            (playerY + 30).toInt(),
            (playerX + player.width - 40).toInt(),
            (playerY + player.height - 20).toInt()
        )
        shieldRect.set(
            shieldX.toInt(),
            shieldY.toInt(),
            (shieldX + shield.width).toInt(),
            (shieldY + shield.height).toInt()
        )

        if (!hasShield && Rect.intersects(playerRect, shieldRect)) {
            hasShield = true
            shieldX = -1000f
        }

        obstacleRect.set(
            (obstacleX + 20).toInt(),
            (obstacleY + 15).toInt(),
            (obstacleX + obstacle.width - 20).toInt(),
            (obstacleY + obstacle.height - 15).toInt()
        )

        if (Rect.intersects(playerRect, obstacleRect)) {

            if (hasShield) {
                hasShield = false
                obstacleX = screenWidth.toFloat() + 500
                obstacleCount++
            } else {
                gameOver = true
                musicPlayer.pause()
            }

            if (score > bestScore) {
                bestScore = score

                prefs.edit()
                    .putInt("best_score", bestScore)
                    .apply()
            }
        }
        // Coin collision
        if (coinActive) {

            coinRect.set(
                coinX.toInt(),
                coinY.toInt(),
                (coinX + coin.width).toInt(),
                (coinY + coin.height).toInt()
            )

            if (Rect.intersects(playerRect, coinRect)) {

                coinActive = false
                coinX = -1000f
                coinY = -1000f

                score += 5

                // Update Level 2 drone speed
                updateDroneSpeed()
            }
        }
        // LEVEL 2 - DRONE COLLISION
        if (currentLevel == 2 && droneActive) {

            val droneRect = Rect(
                droneX.toInt() + 15,
                droneY.toInt() + 15,
                (droneX + drone.width - 15).toInt(),
                (droneY + drone.height - 15).toInt()
            )

            if (Rect.intersects(playerRect, droneRect)) {

                gameOver = true
                droneActive = false
                musicPlayer.pause()

                if (score > bestScore) {
                    bestScore = score

                    prefs.edit()
                        .putInt("best_score", bestScore)
                        .apply()
                }
            }
        }
        // LEVEL 2 - SHURIKEN HITS DRONE
        if (currentLevel == 2 && droneActive && shurikenActive) {

            val droneRect = Rect(
                droneX.toInt(),
                droneY.toInt(),
                (droneX + drone.width).toInt(),
                (droneY + drone.height).toInt()
            )

            val shurikenRect = Rect(
                shurikenX.toInt(),
                shurikenY.toInt(),
                (shurikenX + shuriken.width).toInt(),
                (shurikenY + shuriken.height).toInt()
            )

            if (Rect.intersects(shurikenRect, droneRect)) {

                // Shuriken destroys the drone
                shurikenActive = false

                // Remove shuriken
                shurikenX = -1000f
                shurikenY = -1000f

                // Increase score
                score++

                updateDroneSpeed()

                // Spawn a new drone from the right
                droneActive = true
                droneX = screenWidth + 700f
                droneY = groundY - 350f
            }
        }
        // LEVEL 2 ATTACK BUTTON
        if (gameStarted && !gameOver && currentLevel == 2) {

            shurikenButtonRect.set(
                screenWidth - 350f,
                screenHeight - 270f,
                screenWidth - 30f,
                screenHeight - 80f
            )

            canvas.drawRoundRect(
                shurikenButtonRect,
                30f,
                30f,
                retryPaint
            )

            retryPaint.color = Color.BLACK

            canvas.drawText(
                "SHURIKEN",
                shurikenButtonRect.centerX(),
                shurikenButtonRect.centerY() + 20f,
                retryPaint
            )

            retryPaint.color = Color.WHITE
        }

        if (gameOver) {

            canvas.drawRect(
                0f,
                0f,
                screenWidth.toFloat(),
                screenHeight.toFloat(),
                overlayPaint
            )

            // Bigger Game Over box
            val gameOverBox = android.graphics.RectF(
                screenWidth / 2f - 420f,
                screenHeight / 2f - 300f,
                screenWidth / 2f + 420f,
                screenHeight / 2f + 430f
            )

            canvas.drawRoundRect(
                gameOverBox,
                40f,
                40f,
                gameOverBoxPaint
            )

            // GAME OVER
            canvas.drawText(
                "GAME OVER",
                screenWidth / 2f,
                screenHeight / 2f - 170f,
                gameOverPaint
            )

            // TOO BAD
            canvas.drawText(
                "TOO BAD!!",
                screenWidth / 2f,
                screenHeight / 2f - 80f,
                tooBadPaint
            )

            // SCORE
            canvas.drawText(
                "Score : $score",
                screenWidth / 2f,
                screenHeight / 2f - 10f,
                gameOverScorePaint
            )

            // BEST
            canvas.drawText(
                "Best : $bestScore",
                screenWidth / 2f,
                screenHeight / 2f + 60f,
                gameOverScorePaint
            )

            // -------------------------
            // RETRY BUTTON
            // -------------------------

            retryRect.set(
                screenWidth / 2f - 180f,
                screenHeight / 2f + 130f,
                screenWidth / 2f + 180f,
                screenHeight / 2f + 240f
            )

            retryPaint.color = Color.WHITE
            retryPaint.textSize = 55f

            canvas.drawRoundRect(
                retryRect,
                30f,
                30f,
                retryPaint
            )

            retryPaint.color = Color.BLACK

            canvas.drawText(
                "RETRY",
                retryRect.centerX(),
                retryRect.centerY() + 20f,
                retryPaint
            )

            // -------------------------
            // HOME BUTTON
            // -------------------------

            homeRect.set(
                screenWidth / 2f - 180f,
                screenHeight / 2f + 275f,
                screenWidth / 2f + 180f,
                screenHeight / 2f + 385f
            )

            retryPaint.color = Color.WHITE

            canvas.drawRoundRect(
                homeRect,
                30f,
                30f,
                retryPaint
            )

            retryPaint.color = Color.BLACK

            canvas.drawText(
                "HOME",
                homeRect.centerX(),
                homeRect.centerY() + 20f,
                retryPaint
            )

            retryPaint.color = Color.WHITE
            retryPaint.textSize = 70f
        }
            postInvalidateOnAnimation()
        if (!gameStarted) {

            // START SCREEN BACKGROUND
            canvas.drawBitmap(
                startBackground,
                0f,
                0f,
                null
            )

            // DARK OVERLAY
            canvas.drawRect(
                0f,
                0f,
                screenWidth.toFloat(),
                screenHeight.toFloat(),
                overlayPaint
            )

            // TITLE
            canvas.drawText(
                "JUMPER NINJA",
                screenWidth / 2f,
                screenHeight / 4f,
                gameOverPaint
            )

            // BEST SCORE
            canvas.drawText(
                "BEST : $bestScore",
                screenWidth / 2f,
                screenHeight / 3f,
                gameOverScorePaint
            )

            // -------------------------
            // LEVEL 1 BUTTON
            // -------------------------

            level1ButtonRect.set(
                screenWidth / 2f - 200f,
                screenHeight / 2f - 40f,
                screenWidth / 2f + 200f,
                screenHeight / 2f + 80f
            )

            canvas.drawRoundRect(
                level1ButtonRect,
                30f,
                30f,
                retryPaint
            )

            retryPaint.color = Color.BLACK

            canvas.drawText(
                "LEVEL 1",
                level1ButtonRect.centerX(),
                level1ButtonRect.centerY() + 25f,
                retryPaint
            )

            // -------------------------
            // LEVEL 2 BUTTON
            // -------------------------

            level2ButtonRect.set(
                screenWidth / 2f - 200f,
                screenHeight / 2f + 110f,
                screenWidth / 2f + 200f,
                screenHeight / 2f + 230f
            )

// Make button WHITE
            retryPaint.color = Color.WHITE

            canvas.drawRoundRect(
                level2ButtonRect,
                30f,
                30f,
                retryPaint
            )

// Make text BLACK
            retryPaint.color = Color.BLACK

            canvas.drawText(
                "LEVEL 2",
                level2ButtonRect.centerX(),
                level2ButtonRect.centerY() + 25f,
                retryPaint
            )

            retryPaint.color = Color.WHITE
        }
        }
    }

