package com.example.conejojorge

/** Dificultad progresiva: con más puntos caen más pinchos y más rápido. */
object Difficulty {
    const val BASE_SPIKES = 3
    const val MAX_SPIKES = 6
    /** Cada cuántos puntos aparece un pincho más. */
    const val POINTS_PER_EXTRA_SPIKE = 300

    const val MAX_SPEED_MULTIPLIER = 2f
    /** Puntos a partir de los cuales los pinchos caen a la velocidad máxima. */
    const val POINTS_FOR_MAX_SPEED = 1500

    fun spikeCount(points: Int): Int =
        (BASE_SPIKES + points / POINTS_PER_EXTRA_SPIKE).coerceAtMost(MAX_SPIKES)

    fun speedMultiplier(points: Int): Float {
        val progress = (points.toFloat() / POINTS_FOR_MAX_SPEED).coerceIn(0f, 1f)
        return 1f + (MAX_SPEED_MULTIPLIER - 1f) * progress
    }
}
