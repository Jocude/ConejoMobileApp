package com.example.conejojorge

import kotlin.random.Random

/** Pincho que cae desde arriba. Solo guarda estado: las imágenes están en [GameSprites]. */
class Spike(private val random: Random) {
    var x = 0f
    var y = 0f
    var velocity = 0f // píxeles por segundo
        private set
    var frame = 0
        private set
    private var frameTime = 0f

    fun reset(screenWidth: Int, screenHeight: Int, spikeWidth: Int) {
        x = random.nextInt((screenWidth - spikeWidth).coerceAtLeast(1)).toFloat()
        y = -100f - random.nextInt(200)
        val fallSeconds = MIN_FALL_SECONDS + random.nextFloat() * (MAX_FALL_SECONDS - MIN_FALL_SECONDS)
        velocity = screenHeight / fallSeconds
    }

    fun animate(deltaSeconds: Float) {
        frameTime += deltaSeconds
        while (frameTime >= FRAME_SECONDS) {
            frameTime -= FRAME_SECONDS
            frame = (frame + 1) % FRAME_COUNT
        }
    }

    companion object {
        // Tiempo (en segundos) que tarda un pincho en recorrer toda la pantalla
        const val MIN_FALL_SECONDS = 1.5f
        const val MAX_FALL_SECONDS = 3f
        const val FRAME_SECONDS = 0.033f
        const val FRAME_COUNT = 3
    }
}

/**
 * Indica si un pincho ha tocado al conejo en este frame. Se comprueba todo el tramo recorrido
 * (desde [spikeTopBefore] hasta [spikeBottomAfter]) para que un pincho rápido no atraviese al conejo.
 */
fun spikeHitsRabbit(
    spikeX: Float, spikeWidth: Int, spikeTopBefore: Float, spikeBottomAfter: Float,
    rabbitX: Float, rabbitY: Float, rabbitWidth: Int, rabbitHeight: Int,
): Boolean =
    spikeX + spikeWidth >= rabbitX &&
        spikeX <= rabbitX + rabbitWidth &&
        spikeBottomAfter >= rabbitY &&
        spikeTopBefore <= rabbitY + rabbitHeight
