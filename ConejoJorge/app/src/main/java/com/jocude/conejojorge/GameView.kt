package com.jocude.conejojorge

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.view.Choreographer
import android.view.MotionEvent
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.random.Random

/**
 * Motor del juego. El bucle va con [Choreographer] (sincronizado con el refresco de la pantalla):
 * en cada frame se actualiza la lógica con el tiempo real transcurrido ([update]) y después se dibuja ([onDraw]).
 *
 * Todo se escala con [unit] (ancho de pantalla / [REFERENCE_WIDTH]), así el juego se ve igual
 * en cualquier móvil, independientemente de su resolución y densidad.
 */
@SuppressLint("ViewConstructor")
class GameView(context: Context) : View(context), Choreographer.FrameCallback {

    /** Se llama una sola vez, al perder la última vida. */
    var onGameOver: ((points: Int) -> Unit)? = null

    private val sprites = GameSprites(resources)
    private val audio = GameAudio(context)
    private val random = Random.Default
    private val spikes = mutableListOf<Spike>()
    private val explosions = mutableListOf<Explosion>()

    private var points = 0
    private var lives = MAX_LIVES
    private var gameOver = false
    /** Pausa visible para el jugador (botón, salir de la app o botón atrás). */
    private var paused = false
    private var loopRunning = false
    private var lastFrameNanos = 0L

    // Tamaño de la vista y zonas ocupadas por las barras del sistema (pantalla de borde a borde)
    private var screenWidth = 0
    private var screenHeight = 0
    private var insetTop = 0
    private var insetBottom = 0
    private var sceneReady = false
    /** Píxeles de pantalla por cada píxel de los sprites originales. */
    private var unit = 1f
    private var groundTop = 0f

    private var rabbitX = 0f
    private var rabbitY = 0f
    private var dragging = false
    private var touchStartX = 0f
    private var rabbitStartX = 0f
    /** Evita que el mismo toque que pausa la partida la reanude al levantar el dedo. */
    private var resumeArmed = false

    // --- Pinceles ---
    private val spritePaint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
    private val groundFillPaint = Paint().apply { color = sprites.groundFillColor }
    private val gameFont: Typeface = resources.getFont(R.font.fredoka_bold)
    private val scoreFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(255, 209, 102)
        typeface = gameFont
    }
    private val scoreStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(59, 42, 63)
        typeface = gameFont
        style = Paint.Style.STROKE
    }
    private val lifeSlotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(110, 0, 0, 0) }
    private val lifePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val buttonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(120, 0, 0, 0) }
    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
    private val overlayPaint = Paint().apply { color = Color.argb(140, 0, 0, 0) }
    private val overlayText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = gameFont
    }
    private val rect = RectF()

    private val pausedLabel = context.getString(R.string.paused)
    private val tapToContinueLabel = context.getString(R.string.tap_to_continue)

    init {
        contentDescription = context.getString(R.string.app_name)
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

    /** Calcula la escala, coloca el suelo por encima de la barra de navegación y el conejo sobre el suelo. */
    private fun layoutScene() {
        if (screenWidth == 0 || screenHeight == 0) return
        unit = screenWidth / REFERENCE_WIDTH
        scoreFill.textSize = SCORE_TEXT_SIZE * unit
        scoreStroke.textSize = SCORE_TEXT_SIZE * unit
        scoreStroke.strokeWidth = 5f * unit
        groundTop = screenHeight - insetBottom - GameSprites.GROUND_HEIGHT * unit
        rabbitY = groundTop - rabbitHeight
        if (!sceneReady) {
            rabbitX = (screenWidth - rabbitWidth) / 2f
            repeat(Difficulty.BASE_SPIKES) { spikes.add(newSpike()) }
            sceneReady = true
        } else {
            rabbitX = rabbitX.coerceIn(0f, maxRabbitX)
        }
    }

    private val rabbitWidth get() = GameSprites.RABBIT_WIDTH * unit
    private val rabbitHeight get() = GameSprites.RABBIT_HEIGHT * unit
    private val spikeWidth get() = GameSprites.SPIKE_SIZE * unit
    private val spikeHeight get() = GameSprites.SPIKE_SIZE * unit
    private val maxRabbitX get() = (screenWidth - rabbitWidth).coerceAtLeast(0f)

    private fun newSpike() = Spike(random).also { resetSpike(it) }

    private fun resetSpike(spike: Spike) =
        spike.reset(screenWidth, screenHeight, spikeWidth, spikeHeight, Difficulty.speedMultiplier(points))

    // --- Bucle del juego y pausa ---

    /** Pausa la partida y muestra la pantalla de pausa. */
    fun pauseGame() {
        if (gameOver) return
        paused = true
        resumeArmed = false
        dragging = false
        stopLoop()
        invalidate()
    }

    fun resumeGame() {
        if (gameOver) return
        paused = false
        startLoop()
    }

    private fun startLoop() {
        if (loopRunning || paused || gameOver || !isAttachedToWindow) return
        loopRunning = true
        lastFrameNanos = 0L // evita un salto al volver de la pausa
        Choreographer.getInstance().postFrameCallback(this)
    }

    private fun stopLoop() {
        loopRunning = false
        Choreographer.getInstance().removeFrameCallback(this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        ViewCompat.requestApplyInsets(this)
        startLoop()
    }

    override fun onDetachedFromWindow() {
        stopLoop()
        audio.release()
        super.onDetachedFromWindow()
    }

    /** Si se despliega la barra de notificaciones o salta un diálogo, la partida se pausa. */
    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        if (!hasWindowFocus && !paused) pauseGame()
    }

    override fun doFrame(frameTimeNanos: Long) {
        if (!loopRunning) return
        val deltaSeconds = if (lastFrameNanos == 0L) 0f
        else ((frameTimeNanos - lastFrameNanos) / 1_000_000_000f).coerceAtMost(MAX_DELTA_SECONDS)
        lastFrameNanos = frameTimeNanos
        update(deltaSeconds)
        invalidate()
        if (loopRunning) Choreographer.getInstance().postFrameCallback(this)
    }

    // --- Lógica ---

    private fun update(deltaSeconds: Float) {
        if (!sceneReady || gameOver) return

        // Caja de choque del conejo, algo más pequeña que la imagen (tiene los bordes redondeados)
        val insetX = rabbitWidth * RABBIT_HITBOX_INSET
        val insetY = rabbitHeight * RABBIT_HITBOX_INSET
        val rabbitLeft = rabbitX + insetX
        val rabbitTop = rabbitY + insetY
        val rabbitRight = rabbitX + rabbitWidth - insetX
        val rabbitBottom = rabbitY + rabbitHeight
        val radius = spikeWidth * Spike.BODY_RADIUS_RATIO

        for (spike in spikes) {
            spike.animate(deltaSeconds)
            val previousY = spike.y
            spike.y += spike.velocity * deltaSeconds
            val hit = spikeHitsRabbit(
                centerX = spike.x + spikeWidth / 2,
                centerYBefore = previousY + spikeHeight / 2,
                centerYAfter = spike.y + spikeHeight / 2,
                radius = radius,
                left = rabbitLeft, top = rabbitTop, right = rabbitRight, bottom = rabbitBottom,
            )
            if (hit) {
                lives--
                audio.playHit()
                resetSpike(spike)
                if (lives <= 0) {
                    endGame()
                    return
                }
            } else if (spike.y + spikeHeight >= groundTop) {
                points += POINTS_PER_SPIKE
                audio.playPop()
                explosions.add(Explosion(spike.x, spike.y))
                resetSpike(spike)
            }
        }

        // Dificultad progresiva: se añaden pinchos según los puntos
        while (spikes.size < Difficulty.spikeCount(points)) spikes.add(newSpike())

        // Iterator: permite borrar mientras se recorre sin saltarse ninguna explosión
        val iterator = explosions.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().advance(deltaSeconds)) iterator.remove()
        }
    }

    private fun endGame() {
        lives = 0
        gameOver = true
        stopLoop()
        audio.vibrateGameOver()
        onGameOver?.invoke(points)
    }

    // --- Dibujo ---

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!sceneReady) return
        drawBackground(canvas)
        drawGround(canvas)
        drawSprite(canvas, sprites.rabbit, rabbitX, rabbitY, rabbitWidth, rabbitHeight)
        for (spike in spikes) drawSprite(canvas, sprites.spikeFrames[spike.frame], spike.x, spike.y, spikeWidth, spikeHeight)
        val explosionWidth = GameSprites.EXPLOSION_WIDTH * unit
        val explosionHeight = GameSprites.EXPLOSION_HEIGHT * unit
        for (explosion in explosions) {
            drawSprite(canvas, sprites.explosionFrames[explosion.frame], explosion.x, explosion.y, explosionWidth, explosionHeight)
        }
        drawHud(canvas)
        if (paused) drawPauseOverlay(canvas)
    }

    /** Dibuja un sprite con el tamaño indicado (en píxeles de pantalla) y suavizado. */
    private fun drawSprite(canvas: Canvas, bitmap: Bitmap, x: Float, y: Float, width: Float, height: Float) {
        rect.set(x, y, x + width, y + height)
        canvas.drawBitmap(bitmap, null, rect, spritePaint)
    }

    /** Fondo escalado para cubrir la pantalla sin deformarse (se recorta lo que sobra). */
    private fun drawBackground(canvas: Canvas) {
        val bg = sprites.background
        val scale = maxOf(screenWidth.toFloat() / bg.width, screenHeight.toFloat() / bg.height)
        val w = bg.width * scale
        val h = bg.height * scale
        rect.set((screenWidth - w) / 2, (screenHeight - h) / 2, (screenWidth + w) / 2, (screenHeight + h) / 2)
        canvas.drawBitmap(bg, null, rect, spritePaint)
    }

    /** Suelo repetido a lo ancho (sin estirarse) y relleno por debajo, detrás de la barra de navegación. */
    private fun drawGround(canvas: Canvas) {
        val tile = sprites.groundTile
        val tileWidth = GameSprites.GROUND_TILE_WIDTH * unit
        val tileHeight = GameSprites.GROUND_HEIGHT * unit
        // El relleno empieza algo antes del final del suelo para que no quede ninguna rendija entre ambos
        canvas.drawRect(0f, groundTop + tileHeight * 0.75f, screenWidth.toFloat(), screenHeight.toFloat(), groundFillPaint)
        var x = 0f
        while (x < screenWidth) {
            rect.set(x, groundTop, x + tileWidth, groundTop + tileHeight)
            canvas.drawBitmap(sprites.ground, tile, rect, spritePaint)
            x += tileWidth - 1 // solapa 1 px para que no se vean costuras
        }
    }

    private fun drawHud(canvas: Canvas) {
        val top = insetTop + HUD_MARGIN * unit

        // Puntuación con contorno para que se lea sobre el cielo
        val baseline = top + SCORE_TEXT_SIZE * unit * 0.8f
        val text = points.toString()
        canvas.drawText(text, HUD_MARGIN * unit, baseline, scoreStroke)
        canvas.drawText(text, HUD_MARGIN * unit, baseline, scoreFill)

        // Vidas: 3 casillas que se vacían (verde → amarillo → rojo)
        lifePaint.color = when (lives) {
            3 -> Color.rgb(76, 175, 80)
            2 -> Color.rgb(255, 193, 7)
            else -> Color.rgb(244, 67, 54)
        }
        val slot = LIFE_SLOT_SIZE * unit
        val gap = 4f * unit
        val corner = 4f * unit
        var x = screenWidth - HUD_MARGIN * unit - MAX_LIVES * slot - (MAX_LIVES - 1) * gap
        for (i in 0 until MAX_LIVES) {
            rect.set(x, top, x + slot, top + slot)
            canvas.drawRoundRect(rect, corner, corner, if (i < lives) lifePaint else lifeSlotPaint)
            x += slot + gap
        }

        // Botón de pausa arriba en el centro
        val cx = screenWidth / 2f
        val cy = pauseButtonCenterY
        val r = PAUSE_BUTTON_RADIUS * unit
        canvas.drawCircle(cx, cy, r, buttonPaint)
        val barW = r * 0.22f
        val barH = r * 0.9f
        rect.set(cx - barW * 1.8f, cy - barH / 2, cx - barW * 0.8f, cy + barH / 2)
        canvas.drawRoundRect(rect, barW / 3, barW / 3, iconPaint)
        rect.set(cx + barW * 0.8f, cy - barH / 2, cx + barW * 1.8f, cy + barH / 2)
        canvas.drawRoundRect(rect, barW / 3, barW / 3, iconPaint)
    }

    private val pauseButtonCenterY get() = insetTop + (HUD_MARGIN + PAUSE_BUTTON_RADIUS) * unit

    private fun drawPauseOverlay(canvas: Canvas) {
        canvas.drawRect(0f, 0f, screenWidth.toFloat(), screenHeight.toFloat(), overlayPaint)
        val cy = screenHeight / 2f
        overlayText.textSize = 48f * unit
        canvas.drawText(pausedLabel, screenWidth / 2f, cy, overlayText)
        overlayText.textSize = 20f * unit
        canvas.drawText(tapToContinueLabel, screenWidth / 2f, cy + 40f * unit, overlayText)
    }

    // --- Control táctil ---

    private fun isOnPauseButton(x: Float, y: Float): Boolean {
        val dx = x - screenWidth / 2f
        val dy = y - pauseButtonCenterY
        val r = PAUSE_BUTTON_TOUCH_RADIUS * unit
        return dx * dx + dy * dy <= r * r
    }

    /**
     * Arrastrar en la mitad inferior de la pantalla mueve al conejo; el botón de arriba pausa.
     * En pausa, un toque en cualquier sitio reanuda la partida.
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (gameOver || !sceneReady) return true
        if (paused) {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> resumeArmed = true
                MotionEvent.ACTION_UP -> if (resumeArmed) resumeGame()
            }
            return true
        }
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (isOnPauseButton(event.x, event.y)) {
                    pauseGame()
                    return true
                }
                dragging = event.y >= screenHeight * CONTROL_ZONE_START
                touchStartX = event.x
                rabbitStartX = rabbitX
            }
            MotionEvent.ACTION_MOVE -> if (dragging) {
                rabbitX = (rabbitStartX + event.x - touchStartX).coerceIn(0f, maxRabbitX)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> dragging = false
        }
        return true
    }

    companion object {
        /** Ancho de referencia (en píxeles de los sprites): con 392 se ve igual que antes en un móvil típico. */
        private const val REFERENCE_WIDTH = 392f
        private const val MAX_LIVES = 3
        private const val POINTS_PER_SPIKE = 10
        /** Margen de la caja de choque del conejo respecto a su imagen (por cada lado, salvo abajo). */
        private const val RABBIT_HITBOX_INSET = 0.08f
        /** A partir de qué altura (fracción de la pantalla) se puede arrastrar al conejo. */
        private const val CONTROL_ZONE_START = 0.5f

        // HUD, en unidades de sprite (se multiplican por [unit])
        private const val HUD_MARGIN = 14f
        private const val SCORE_TEXT_SIZE = 40f
        private const val LIFE_SLOT_SIZE = 20f
        private const val PAUSE_BUTTON_RADIUS = 20f
        private const val PAUSE_BUTTON_TOUCH_RADIUS = 32f

        // Paso máximo de simulación: si el móvil se atasca, evita que los pinchos den un salto enorme
        private const val MAX_DELTA_SECONDS = 0.05f
    }
}
