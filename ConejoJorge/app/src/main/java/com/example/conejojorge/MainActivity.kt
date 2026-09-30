package com.example.conejojorge

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var gameView: GameView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    fun startGame(view: View) {
        val game = GameView(this)
        game.onGameOver = { points ->
            startActivity(Intent(this, GameOver::class.java).putExtra(GameOver.EXTRA_POINTS, points))
            finish()
        }
        gameView = game
        setContentView(game)
    }

    // La partida se pausa al salir de la app y continúa al volver
    override fun onPause() {
        super.onPause()
        gameView?.pause()
    }

    override fun onResume() {
        super.onResume()
        gameView?.resume()
    }
}
