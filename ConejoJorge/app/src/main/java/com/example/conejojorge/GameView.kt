package com.example.conejojorge

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.Choreographer
import android.view.MotionEvent
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.random.Random

/**
 * Motor del juego. El bucle va con [Choreographer] (sincronizado con el refresco de la pantalla):
 * en cada frame se actualiza la lógica con el tiempo real transcurrido ([update]) y después se dibuja ([onDraw]).
 */
@SuppressLint("ViewConstructor")
class GameView(context: Context) : View(context), Choreographer.FrameCallback {

    /** Se llama una sola vez, al perder la última vida. */
    var onGameOver: ((points: Int) -> Unit)? = null

    private val sprites = GameSprites(resources)
    private val random = Random.Default
    private val spikes = List(SPIKE_COUNT) { Spike(random) }
    private val explosions = mutableListOf<Explosion>()

    private val textPaint = Paint().apply {
        color = Color.rgb(255, 165, 0)
        textSize = TEXT_SIZE
        textAlign = Paint.Align.LEFT
    }
    private val healthPaint = Paint()

    private var points = 0
    private var lives = MAX_LIVES
    private var gameOver = false
    private var running = false
    private var lastFrameNanos = 0L

    // Tamaño de la vista y zonas ocupadas por las barras del sistema (pantalla de borde a borde)
    private var screenWidth = 0
    private var screenHeight = 0
    private var insetTop = 0
    private var insetBottom = 0
    private var sceneReady = false
    private val backgroundRect = Rect()
    private val groundRect = Rect()

    private var rabbitX = 0f
    private var rabbitY = 0f
    private var touchStartX = 0f
    private var rabbitStartX = 0f

    init {
        ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            insetTop = bars.top
            insetBottom = bars.bottom
            layoutScene()
            insets
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        screenWidth = w
        screenHeight = h
        layoutScene()
    }

    /** Coloca el suelo por encima de la barra de navegación y el conejo sobre el suelo. */
    private fun layoutScene() {
        if (screenWidth == 0 || screenHeight == 0) return
        backgroundRect.set(0, 0, screenWidth, screenHeight)
        val groundTop = screenHeight - insetBottom - sprites.ground.height
        groundRect.set(0, groundTop, screenWidth, screenHeight)
        rabbitY = (groundTop - sprites.rabbit.height).toFloat()
        if (!sceneReady) {
            rabbitX = (screenWidth - sprites.rabbit.width) / 2f
            spikes.forEach { it.reset(screenWidth, screenHeight, spikeWidth) }
            sceneReady = true
        } else {
            rabbitX = rabbitX.coerceIn(0f, maxRabbitX)
        }
    }

    private val spikeWidth get() = sprites.spikeFrames[0].width
    private val spikeHeight get() = sprites.spikeFrames[0].height
    private val maxRabbitX get() = (screenWidth - sprites.rabbit.width).toFloat().coerceAtLeast(0f)

    // --- Bucle del juego ---

    fun resume() {
        if (running || gameOver) return
        running = true
        lastFrameNanos = 0L // evita un salto al volver de la pausa
        Choreographer.getInstance().postFrameCallback(this)
    }

    fun pause() {
        running = false
        Choreographer.getInstance().removeFrameCallback(this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        ViewCompat.requestApplyInsets(this)
        resume()
    }

    override fun onDetachedFromWindow() {
        pause()
        super.onDetachedFromWindow()
    }

    override fun doFrame(frameTimeNanos: Long) {
        if (!running) return
        val deltaSeconds = if (lastFrameNanos == 0L) 0f
        else ((frameTimeNanos - lastFrameNanos) / 1_000_000_000f).coerceAtMost(MAX_DELTA_SECONDS)
        lastFrameNanos = frameTimeNanos
        update(deltaSeconds)
        invalidate()
        if (running) Choreographer.getInstance().postFrameCallback(this)
    }

    private fun update(deltaSeconds: Float) {
        if (!sceneReady || gameOver) return
        val groundTop = groundRect.top

        for (spike in spikes) {
            spike.animate(deltaSeconds)
            val previousY = spike.y
            spike.y += spike.velocity * deltaSeconds
            val hit = spikeHitsRabbit(
                spike.x, spikeWidth, previousY, spike.y + spikeHeight,
                rabbitX, rabbitY, sprites.rabbit.width, sprites.rabbit.height,
            )
            if (hit) {
                lives--
                spike.reset(screenWidth, screenHeight, spikeWidth)
                if (lives <= 0) {
                    endGame()
                    return
                }
            } else if (spike.y + spikeHeight >= groundTop) {
                points += POINTS_PER_SPIKE
                explosions.add(Explosion(spike.x, spike.y))
                spike.reset(screenWidth, screenHeight, spikeWidth)
            }
        }

        // Iterator: permite borrar mientras se recorre sin saltarse ninguna explosión
        val iterator = explosions.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().advance(deltaSeconds)) iterator.remove()
        }
    }

    private fun endGame() {
        lives = 0
        gameOver = true
        pause()
        onGameOver?.invoke(points)
    }

    // --- Dibujo ---

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(sprites.background, null, backgroundRect, null)
        canvas.drawBitmap(sprites.ground, null, groundRect, null)
        if (!sceneReady) return
        canvas.drawBitmap(sprites.rabbit, rabbitX, rabbitY, null)
        for (spike in spikes) {
            canvas.drawBitmap(sprites.spikeFrames[spike.frame], spike.x, spike.y, null)
        }
        for (explosion in explosions) {
            canvas.drawBitmap(sprites.explosionFrames[explosion.frame], explosion.x, explosion.y, null)
        }
        healthPaint.color = when (lives) {
            3 -> Color.GREEN
            2 -> Color.YELLOW
            else -> Color.RED
        }
        val barLeft = screenWidth - 200f
        canvas.drawRect(barLeft, insetTop + 30f, barLeft + 60f * lives, insetTop + 80f, healthPaint)
        canvas.drawText(points.toString(), 20f, insetTop + TEXT_SIZE, textPaint)
    }

    // --- Control táctil: arrastrar en la parte baja de la pantalla mueve al conejo ---

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.y >= rabbitY) {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    touchStartX = event.x
                    rabbitStartX = rabbitX
                }
                MotionEvent.ACTION_MOVE -> {
                    rabbitX = (rabbitStartX + event.x - touchStartX).coerceIn(0f, maxRabbitX)
                }
            }
        }
        return true
    }

    companion object {
        private const val SPIKE_COUNT = 3
        private const val MAX_LIVES = 3
        private const val POINTS_PER_SPIKE = 10
        private const val TEXT_SIZE = 120f
        // Paso máximo de simulación: si el móvil se atasca, evita que los pinchos den un salto enorme
        private const val MAX_DELTA_SECONDS = 0.05f
    }
}
