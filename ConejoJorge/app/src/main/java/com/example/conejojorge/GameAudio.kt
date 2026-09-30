package com.example.conejojorge

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/** Efectos de sonido y vibración de la partida. El volumen es el de multimedia del móvil. */
class GameAudio(context: Context) {
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()
    private val hitSound = soundPool.load(context, R.raw.hit, 1)
    private val popSound = soundPool.load(context, R.raw.pop, 1)

    private val vibrator: Vibrator? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Vibrator::class.java)
        }

    /** Un pincho golpea al conejo. */
    fun playHit() {
        soundPool.play(hitSound, 1f, 1f, 1, 0, 1f)
        vibrate(HIT_VIBRATION_MS)
    }

    /** Un pincho llega al suelo (suena bajito para no cansar). */
    fun playPop() {
        soundPool.play(popSound, POP_VOLUME, POP_VOLUME, 0, 0, 1f)
    }

    fun vibrateGameOver() = vibrate(GAME_OVER_VIBRATION_MS)

    private fun vibrate(millis: Long) {
        val v = vibrator ?: return
        if (v.hasVibrator()) v.vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    fun release() = soundPool.release()

    private companion object {
        const val POP_VOLUME = 0.35f
        const val HIT_VIBRATION_MS = 60L
        const val GAME_OVER_VIBRATION_MS = 250L
    }
}
