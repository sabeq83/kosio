package com.kosio.app.data.ads

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OcrQuotaLogicTests {

    data class MockQuotaState(
        var dailyFreeQuota: Int = 3,
        var dailyUsed: Int = 0,
        var bonusQuota: Int = 0
    ) {
        val totalAvailable: Int
            get() = maxOf(0, dailyFreeQuota - dailyUsed) + bonusQuota

        fun consume(): Boolean {
            return if (dailyUsed < dailyFreeQuota) {
                dailyUsed++
                true
            } else if (bonusQuota > 0) {
                bonusQuota--
                true
            } else {
                false
            }
        }

        fun addBonus(amount: Int = 3) {
            bonusQuota += amount
        }
    }

    @Test
    fun testInitialDailyQuotaHas3Scans() {
        val state = MockQuotaState()
        assertEquals(3, state.totalAvailable)
        assertEquals(0, state.dailyUsed)
        assertEquals(0, state.bonusQuota)
    }

    @Test
    fun testConsumeDailyQuota() {
        val state = MockQuotaState()
        assertTrue(state.consume())
        assertEquals(1, state.dailyUsed)
        assertEquals(2, state.totalAvailable)

        assertTrue(state.consume())
        assertTrue(state.consume())
        assertEquals(3, state.dailyUsed)
        assertEquals(0, state.totalAvailable)

        // 4th consume fails
        assertFalse(state.consume())
        assertEquals(3, state.dailyUsed)
        assertEquals(0, state.totalAvailable)
    }

    @Test
    fun testAddBonusQuotaAndConsume() {
        val state = MockQuotaState(dailyUsed = 3)
        assertEquals(0, state.totalAvailable)
        assertFalse(state.consume())

        // Reward earned (+3)
        state.addBonus(3)
        assertEquals(3, state.bonusQuota)
        assertEquals(3, state.totalAvailable)

        // Can now consume bonus
        assertTrue(state.consume())
        assertEquals(2, state.bonusQuota)
        assertEquals(2, state.totalAvailable)

        assertTrue(state.consume())
        assertTrue(state.consume())
        assertEquals(0, state.bonusQuota)
        assertEquals(0, state.totalAvailable)

        // No more quota
        assertFalse(state.consume())
    }

    @Test
    fun testAdMobManagerTestUnitIdsAreConfigured() {
        assertTrue(AdMobManager.TEST_REWARDED_AD_UNIT_ID.isNotBlank())
        assertTrue(AdMobManager.TEST_INTERSTITIAL_AD_UNIT_ID.isNotBlank())
        assertTrue(AdMobManager.TEST_NATIVE_AD_UNIT_ID.isNotBlank())
    }
}
