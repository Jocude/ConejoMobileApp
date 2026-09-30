package com.example.conejojorge

import org.junit.Assert.assertEquals
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
            spike.reset(screenWidth = 1080, screenHeight = screenHeight, spikeWidth = 185f, spikeHeight = 195f)
            val fallSeconds = screenHeight / spike.velocity
            // Pequeño margen por el redondeo de los float
            assertTrue(fallSeconds in (Spike.MIN_FALL_SECONDS - 0.001f)..(Spike.MAX_FALL_SECONDS + 0.001f))
            assertTrue(spike.x >= 0f && spike.x <= 1080 - 185f)
            assertTrue(spike.y < 0f)
        }
    }

    @Test
    fun laDificultadMultiplicaLaVelocidad() {
        val normal = Spike(Random(7)).apply { reset(1080, 2400, 185f, 195f, speedMultiplier = 1f) }
        val rapido = Spike(Random(7)).apply { reset(1080, 2400, 185f, 195f, speedMultiplier = 2f) }
        assertEquals(normal.velocity * 2, rapido.velocity, 0.01f)
    }

    // Conejo con caja de choque de x 500-650 e y 1900-2100; pincho con radio 60

    @Test
    fun pinchoEncimaDelConejoLeGolpea() {
        assertTrue(hit(centerX = 575f, before = 1850f, after = 1860f))
    }

    @Test
    fun pinchoRapidoQueAtraviesaAlConejoEnUnFrameLeGolpea() {
        // Antes del frame está por encima del conejo y después ya por debajo
        assertTrue(hit(centerX = 575f, before = 1700f, after = 2300f))
    }

    @Test
    fun pinchoAlLadoNoGolpea() {
        assertFalse(hit(centerX = 300f, before = 1950f, after = 1960f))
    }

    @Test
    fun pinchoTodaviaArribaNoGolpea() {
        assertFalse(hit(centerX = 575f, before = 1000f, after = 1100f))
    }

    @Test
    fun laPuntaQueRozaLaEsquinaNoCuentaComoGolpe() {
        // El centro está en diagonal a la esquina: a 50 px en x y 50 px en y (unos 71 px > radio 60)
        assertFalse(hit(centerX = 450f, before = 1840f, after = 1850f))
    }

    @Test
    fun elCuerpoQueTocaLaEsquinaSiCuentaComoGolpe() {
        // A 30 px en x y 30 px en y de la esquina (unos 42 px < radio 60)
        assertTrue(hit(centerX = 470f, before = 1860f, after = 1870f))
    }

    private fun hit(centerX: Float, before: Float, after: Float) =
        spikeHitsRabbit(centerX, before, after, radius = 60f, left = 500f, top = 1900f, right = 650f, bottom = 2100f)
}
