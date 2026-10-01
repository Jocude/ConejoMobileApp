package com.jocude.conejojorge

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect

/**
 * Imágenes del juego. Se cargan una sola vez por partida, a su tamaño original (sin el escalado
 * automático por densidad). Su tamaño en pantalla no depende de sus píxeles, sino de las medidas
 * de diseño de [Companion] (en unidades de sprite), que [GameView] multiplica por su escala.
 * Así se pueden usar imágenes en alta resolución sin cambiar la jugabilidad.
 */
class GameSprites(private val resources: Resources) {
    private val options = BitmapFactory.Options().apply { inScaled = false }
    private fun load(id: Int): Bitmap = BitmapFactory.decodeResource(resources, id, options)

    val background = load(R.drawable.background)
    val ground = load(R.drawable.ground)
    val rabbit = load(R.drawable.rabbit)
    val spikeFrames = listOf(R.drawable.spike0, R.drawable.spike1, R.drawable.spike2).map(::load)
    val explosionFrames = listOf(R.drawable.explode0, R.drawable.explode1, R.drawable.explode2).map(::load)

    /** Color de la tierra (abajo en el centro del suelo), para rellenar la zona de la barra de navegación. */
    val groundFillColor = ground.getPixel(ground.width / 2, ground.height - 2)

    /** Parte del suelo que se repite: la imagen entera, ya preparada para repetirse sin costuras. */
    val groundTile = Rect(0, 0, ground.width, ground.height)

    companion object {
        // Medidas de diseño, en unidades de sprite (se multiplican por la escala de GameView)
        const val RABBIT_WIDTH = 62f
        const val RABBIT_HEIGHT = 80f
        const val SPIKE_SIZE = 68f
        const val EXPLOSION_WIDTH = 80f
        const val EXPLOSION_HEIGHT = 72f
        const val GROUND_TILE_WIDTH = 96f
        const val GROUND_HEIGHT = 20f
    }
}
