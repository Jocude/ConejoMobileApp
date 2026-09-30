package com.example.conejojorge

/** Animación de explosión cuando un pincho llega al suelo. */
class Explosion(val x: Float, val y: Float) {
    private var step = 0
    private var frameTime = 0f

    /** Fotograma a dibujar: el último se mantiene un paso más, como en la versión original. */
    val frame: Int
        get() = minOf(step, FRAME_COUNT - 1)

    /** Avanza la animación y devuelve true cuando ha terminado. */
    fun advance(deltaSeconds: Float): Boolean {
        frameTime += deltaSeconds
        while (frameTime >= FRAME_SECONDS) {
            frameTime -= FRAME_SECONDS
            step++
        }
        return step >= TOTAL_STEPS
    }

    companion object {
        const val FRAME_SECONDS = 0.033f
        const val FRAME_COUNT = 3
        private const val TOTAL_STEPS = 4
    }
}
