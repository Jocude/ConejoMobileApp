package com.example.conejojorge

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect

/**
 * Imágenes del juego. Se cargan una sola vez por partida, a su tamaño original (sin el escalado
 * automático por densidad), y [GameView] las escala según el ancho de la pantalla.
 */
class GameSprites(private val resources: Resources) {
    private val options = BitmapFactory.Options().apply { inScaled = false }
    private fun load(id: Int): Bitmap = BitmapFactory.decodeResource(resources, id, options)

    val background = load(R.drawable.background)
    val ground = load(R.drawable.ground)
    val rabbit = load(R.drawable.rabbit)
    val spikeFrames = listOf(R.drawable.spike0, R.drawable.spike1, R.drawable.spike2).map(::load)
    val explosionFrames = listOf(R.drawable.explode0, R.drawable.explode1, R.drawable.explode2).map(::load)

    /** Color de la tierra (centro del suelo), para rellenar la zona de la barra de navegación. */
    val groundFillColor = ground.getPixel(ground.width / 2, ground.height / 2)

    /** Parte del suelo que se repite: sin las 2 columnas de la derecha, casi transparentes (dejarían costuras). */
    val groundTile = Rect(0, 0, ground.width - 2, ground.height)
}
