package com.kosio.app.data.ads

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

data class OcrQuotaState(
    val dailyFreeQuota: Int = 3,
    val dailyUsed: Int = 0,
    val bonusQuota: Int = 0,
    val totalAvailable: Int = 3
)

class OcrQuotaManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    private val _quotaState = MutableStateFlow(loadCurrentState())
    val quotaState: StateFlow<OcrQuotaState> = _quotaState.asStateFlow()

    companion object {
        private const val PREFS_NAME = "kosio_ocr_quota_prefs"
        private const val KEY_LAST_RESET_DAY = "key_last_reset_day"
        private const val KEY_DAILY_USED = "key_daily_used"
        private const val KEY_BONUS_QUOTA = "key_bonus_quota"
        const val DEFAULT_DAILY_QUOTA = 3
        const val REWARD_BONUS_AMOUNT = 3
    }

    init {
        checkAndResetDaily()
    }

    @Synchronized
    private fun getTodayDayOfYear(): Int {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.YEAR) * 1000 + calendar.get(Calendar.DAY_OF_YEAR)
    }

    @Synchronized
    private fun checkAndResetDaily() {
        val today = getTodayDayOfYear()
        val lastDay = prefs.getInt(KEY_LAST_RESET_DAY, -1)
        if (today != lastDay) {
            prefs.edit()
                .putInt(KEY_LAST_RESET_DAY, today)
                .putInt(KEY_DAILY_USED, 0)
                .apply()
        }
    }

    @Synchronized
    private fun loadCurrentState(): OcrQuotaState {
        checkAndResetDaily()
        val used = prefs.getInt(KEY_DAILY_USED, 0)
        val bonus = prefs.getInt(KEY_BONUS_QUOTA, 0)
        val availableDaily = maxOf(0, DEFAULT_DAILY_QUOTA - used)
        val total = availableDaily + bonus
        return OcrQuotaState(
            dailyFreeQuota = DEFAULT_DAILY_QUOTA,
            dailyUsed = used,
            bonusQuota = bonus,
            totalAvailable = total
        )
    }

    @Synchronized
    fun hasAvailableQuota(): Boolean {
        checkAndResetDaily()
        val used = prefs.getInt(KEY_DAILY_USED, 0)
        val bonus = prefs.getInt(KEY_BONUS_QUOTA, 0)
        val availableDaily = maxOf(0, DEFAULT_DAILY_QUOTA - used)
        return (availableDaily + bonus) > 0
    }

    @Synchronized
    fun consumeQuota(): Boolean {
        checkAndResetDaily()
        var used = prefs.getInt(KEY_DAILY_USED, 0)
        var bonus = prefs.getInt(KEY_BONUS_QUOTA, 0)

        if (used < DEFAULT_DAILY_QUOTA) {
            used++
            prefs.edit().putInt(KEY_DAILY_USED, used).apply()
            _quotaState.value = loadCurrentState()
            return true
        } else if (bonus > 0) {
            bonus--
            prefs.edit().putInt(KEY_BONUS_QUOTA, bonus).apply()
            _quotaState.value = loadCurrentState()
            return true
        }
        return false
    }

    @Synchronized
    fun addBonusQuota(amount: Int = REWARD_BONUS_AMOUNT) {
        checkAndResetDaily()
        val currentBonus = prefs.getInt(KEY_BONUS_QUOTA, 0)
        val newBonus = currentBonus + amount
        prefs.edit().putInt(KEY_BONUS_QUOTA, newBonus).apply()
        _quotaState.value = loadCurrentState()
    }

    @Synchronized
    fun refresh() {
        _quotaState.value = loadCurrentState()
    }
}
