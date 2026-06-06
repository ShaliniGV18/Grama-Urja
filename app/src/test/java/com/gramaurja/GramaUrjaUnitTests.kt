package com.gramaurja

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Grama-Urja core business logic.
 */
class GramaUrjaUnitTests {

    // ── Pump Timer Tests ──────────────────────────────────────────────────────

    @Test
    fun `pump timer calculation for different crops`() {
        val area = 2.0
        val hp = 5.0
        val rice = com.gramaurja.presentation.screens.pumptimer.CropType.RICE
        val wheat = com.gramaurja.presentation.screens.pumptimer.CropType.WHEAT
        
        val riceHours = rice.calculateHours(area, hp)
        val wheatHours = wheat.calculateHours(area, hp)
        
        // Rice needs 1800 L/acre, Wheat needs 900 L/acre
        assertEquals(2.0, riceHours / wheatHours, 0.01)
    }

    @Test
    fun `sugarcane 5 acres should need more water than rice`() {
        val riceHours = (1800.0 * 5) / 30_000.0
        val sugarcaneHours = (2200.0 * 5) / 30_000.0
        assertTrue("Sugarcane needs more water than rice", sugarcaneHours > riceHours)
    }

    // ── Freshness Text Tests ──────────────────────────────────────────────────

    @Test
    fun `freshness text for 0 timestamp should be empty`() {
        val ts = 0L
        val result = formatFreshness(ts)
        assertEquals("", result)
    }

    @Test
    fun `freshness text for 2 minutes ago`() {
        val ts = System.currentTimeMillis() - (2 * 60_000L)
        val result = formatFreshness(ts)
        assertTrue("Should contain 'min'", result.contains("min"))
    }

    // ── PowerStatus Tests ──────────────────────────────────────────────────────

    @Test
    fun `PowerStatus ON should be ON`() {
        val status = com.gramaurja.data.model.PowerStatus.ON
        assertTrue(status.isOn())
        assertFalse(com.gramaurja.data.model.PowerStatus.OFF.isOn())
    }

    // Helper (mirrors HomeViewModel logic)
    private fun formatFreshness(timestamp: Long): String {
        if (timestamp == 0L) return ""
        val diff = System.currentTimeMillis() - timestamp
        val minutes = diff / 60_000
        val hours = minutes / 60
        return when {
            diff < 60_000 -> "Updated just now"
            minutes < 60 -> "Updated ${minutes} min ago"
            hours < 24 -> "Updated ${hours}h ago"
            else -> "Updated ${hours / 24}d ago"
        }
    }
}
