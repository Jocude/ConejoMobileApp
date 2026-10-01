package com.jocude.conejojorge

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

    /** Coloca el pincho por encima de la pantalla con posición y velocidad aleatorias. */
    fun reset(screenWidth: Int, screenHeight: Int, spikeWidth: Float, spikeHeight: Float, speedMultiplier: Float = 1f) {
        x = random.nextFloat() * (screenWidth - spikeWidth).coerceAtLeast(1f)
        y = -spikeHeight - random.nextFloat() * screenHeight * START_SPREAD
        val fallSeconds = MIN_FALL_SECONDS + random.nextFloat() * (MAX_FALL_SECONDS - MIN_FALL_SECONDS)
        velocity = screenHeight / fallSeconds * speedMultiplier
    }

    fun animate(deltaSeconds: Float) {
        frameTime += deltaSeconds
        while (frameTime >= FRAME_SECONDS) {
            frameTime -= FRAME_SECONDS
            frame = (frame + 1) % FRAME_COUNT
        }
    }

    companion object {
        // Tiempo (en segundos) que tarda un pincho en recorrer toda la pantalla, sin contar la dificultad
        const val MIN_FALL_SECONDS = 1.5f
        const val MAX_FALL_SECONDS = 3f
        /** Hasta qué altura por encima de la pantalla puede aparecer (fracción del alto). */
        const val START_SPREAD = 0.1f
        const val FRAME_SECONDS = 0.033f
        const val FRAME_COUNT = 3
        /** Radio del cuerpo redondo del pincho respecto a su ancho: las puntas no cuentan como golpe. */
        const val BODY_RADIUS_RATIO = 0.33f
    }
}

/**
 * Indica si el cuerpo redondo de un pincho ha tocado la caja del conejo en este frame.
 * Se comprueba todo el tramo recorrido (el centro va de [centerYBefore] a [centerYAfter])
 * para que un pincho rápido no atraviese al conejo sin detectarse.
 */
fun spikeHitsRabbit(
    centerX: Float, centerYBefore: Float, centerYAfter: Float, radius: Float,
    left: Float, top: Float, right: Float, bottom: Float,
): Boolean {
    val dx = maxOf(left - centerX, 0f, centerX - right)
    val segmentTop = minOf(centerYBefore, centerYAfter)
    val segmentBottom = maxOf(centerYBefore, centerYAfter)
    val dy = when {
        segmentBottom < top -> top - segmentBottom
        segmentTop > bottom -> segmentTop - bottom
        else -> 0f
    }
    return dx * dx + dy * dy <= radius * radius
}
