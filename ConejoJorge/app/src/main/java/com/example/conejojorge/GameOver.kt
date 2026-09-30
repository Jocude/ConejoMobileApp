package com.example.conejojorge

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit

class GameOver : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game_over)

        val points = intent.getIntExtra(EXTRA_POINTS, 0)
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        var highest = prefs.getInt(KEY_HIGHEST, 0)
        if (points > highest) {
            findViewById<ImageView>(R.id.ivNewHighest).visibility = View.VISIBLE
            highest = points
            prefs.edit { putInt(KEY_HIGHEST, highest) }
        }
        findViewById<TextView>(R.id.tvPoints).text = points.toString()
        findViewById<TextView>(R.id.tvHighest).text = highest.toString()

        if (savedInstanceState == null) {
            MediaPlayer.create(this, R.raw.game_over)?.apply {
                setOnCompletionListener { it.release() }
                start()
            }
        }
    }

    /** Empieza directamente una partida nueva, sin pasar por el menú. */
    fun restart(view: View) {
        startActivity(Intent(this, MainActivity::class.java).putExtra(MainActivity.EXTRA_START_GAME, true))
        finish()
    }

    fun exit(view: View) {
        finish()
    }

    companion object {
        const val EXTRA_POINTS = "points"
        private const val PREFS_NAME = "my_pref"
        private const val KEY_HIGHEST = "highest"
    }
}
