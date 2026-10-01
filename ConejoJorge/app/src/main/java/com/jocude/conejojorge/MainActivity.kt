package com.jocude.conejojorge

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var gameView: GameView? = null

    /** Durante la partida, el botón atrás pregunta antes de salir. */
    private val backDuringGame = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() = confirmExitGame()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onBackPressedDispatcher.addCallback(this, backDuringGame)

        // "Jugar otra vez" desde Game Over entra directamente en una partida nueva
        if (savedInstanceState == null && intent.getBooleanExtra(EXTRA_START_GAME, false)) {
            startGame(null)
        }
    }

    fun startGame(view: View?) {
        val game = GameView(this)
        game.onGameOver = { points ->
            startActivity(Intent(this, GameOver::class.java).putExtra(GameOver.EXTRA_POINTS, points))
            finish()
        }
        gameView = game
        backDuringGame.isEnabled = true
        setContentView(game)
    }

    private fun confirmExitGame() {
        val game = gameView ?: return
        game.pauseGame()
        AlertDialog.Builder(this)
            .setTitle(R.string.exit_game_title)
            .setMessage(R.string.exit_game_message)
            .setPositiveButton(R.string.exit_game_confirm) { _, _ -> showMenu() }
            .setNegativeButton(R.string.exit_game_cancel) { _, _ -> game.resumeGame() }
            .show()
    }

    private fun showMenu() {
        gameView = null
        backDuringGame.isEnabled = false
        setContentView(R.layout.activity_main)
    }

    // Al salir de la app la partida queda en pausa; al volver, se toca la pantalla para seguir
    override fun onPause() {
        super.onPause()
        gameView?.pauseGame()
    }

    companion object {
        const val EXTRA_START_GAME = "start_game"
    }
}
