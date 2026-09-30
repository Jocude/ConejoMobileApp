package com.example.conejojorge

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory

/** Imágenes del juego. Se cargan una sola vez por partida y las comparten todos los pinchos y explosiones. */
class GameSprites(resources: Resources) {
    val background: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.background)
    val ground: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.ground)
    val rabbit: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.rabbit)
    val spikeFrames: List<Bitmap> = listOf(R.drawable.spike0, R.drawable.spike1, R.drawable.spike2)
        .map { BitmapFactory.decodeResource(resources, it) }
    val explosionFrames: List<Bitmap> = listOf(R.drawable.explode0, R.drawable.explode1, R.drawable.explode2)
        .map { BitmapFactory.decodeResource(resources, it) }
}
