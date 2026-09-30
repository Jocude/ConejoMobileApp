package com.example.conejojorge

import org.junit.Assert.assertEquals
import org.junit.Test

class DifficultyTest {

    @Test
    fun alEmpezarHayTresPinchosAVelocidadNormal() {
        assertEquals(3, Difficulty.spikeCount(0))
        assertEquals(1f, Difficulty.speedMultiplier(0), 0.0001f)
    }

    @Test
    fun cadaTrescientosPuntosAparecenMasPinchosHastaSeis() {
        assertEquals(3, Difficulty.spikeCount(290))
        assertEquals(4, Difficulty.spikeCount(300))
        assertEquals(5, Difficulty.spikeCount(600))
        assertEquals(6, Difficulty.spikeCount(900))
        assertEquals(6, Difficulty.spikeCount(10_000))
    }

    @Test
    fun laVelocidadSubeHastaElDobleYNoPasa() {
        assertEquals(1.5f, Difficulty.speedMultiplier(750), 0.0001f)
        assertEquals(2f, Difficulty.speedMultiplier(1500), 0.0001f)
        assertEquals(2f, Difficulty.speedMultiplier(10_000), 0.0001f)
    }
}
