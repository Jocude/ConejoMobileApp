package com.example.conejojorge

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class SpikeTest {

    @Test
    fun velocidadSiempreDentroDelRango() {
        val screenHeight = 2400
        val spike = Spike(Random(42))
        repeat(1_000) {
            spike.reset(screenWidth = 1080, screenHeight = screenHeight, spikeWidth = 67)
            val fallSeconds = screenHeight / spike.velocity
            // Pequeño margen por el redondeo de los float
            assertTrue(fallSeconds in (Spike.MIN_FALL_SECONDS - 0.001f)..(Spike.MAX_FALL_SECONDS + 0.001f))
            assertTrue(spike.x >= 0f && spike.x <= 1080 - 67)
            assertTrue(spike.y < 0f)
        }
    }

    @Test
    fun pinchoEncimaDelConejoLeGolpea() {
        assertTrue(hit(spikeX = 500f, top = 1900f, bottom = 1990f))
    }

    @Test
    fun pinchoRapidoQueAtraviesaAlConejoEnUnFrameLeGolpea() {
        // Antes del frame está por encima del conejo y después ya por debajo
        assertTrue(hit(spikeX = 500f, top = 1700f, bottom = 2200f))
    }

    @Test
    fun pinchoAlLadoNoGolpea() {
        assertFalse(hit(spikeX = 100f, top = 1900f, bottom = 1990f))
    }

    @Test
    fun pinchoTodaviaArribaNoGolpea() {
        assertFalse(hit(spikeX = 500f, top = 1000f, bottom = 1100f))
    }

    // Conejo de 62x80 en x=500, y=1920
    private fun hit(spikeX: Float, top: Float, bottom: Float) =
        spikeHitsRabbit(spikeX, 67, top, bottom, 500f, 1920f, 62, 80)
}
