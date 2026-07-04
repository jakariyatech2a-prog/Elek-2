package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AdminConfig(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("elek_ai_admin_config", Context.MODE_PRIVATE)

    // Flow representing current configuration state to update UI dynamically
    private val _configFlow = MutableStateFlow(loadConfig())
    val configFlow: StateFlow<ConfigState> = _configFlow

    data class ConfigState(
        val appName: String = "Elek ai",
        val splashLogo: String = "ic_sparkles", // icon indicator
        val appLogo: String = "ic_sparkles",
        val watermarkText: String = "Elek",
        val watermarkSize: Float = 12f, // sp/dp size
        val watermarkPosition: String = "Bottom Right", // "Bottom Right", "Bottom Left", "Top Right", "Top Left"
        val watermarkAlpha: Float = 0.5f, // 0.0f to 1.0f transparency
        val appLanguage: String = "English", // English, বাংলা, Hindi, Arabic, Urdu, etc.
        val aiModel: String = "gemini-3.5-flash",
        val isPremium: Boolean = false,
        val isAdsEnabled: Boolean = false, // false by default
        val downloadQuality: String = "720p", // "720p", "1080p", "2K", "4K"
        val rewardTimeSec: Int = 20,
        val isNotificationEnabled: Boolean = true,
        val isWelcomeEmailEnabled: Boolean = true,
        val apiKey: String = "",
        val isMaintenanceMode: Boolean = false,
        val analyticsEnabled: Boolean = true,
        val feedbackEnabled: Boolean = true,
        val serverStatus: String = "Online / Healthy"
    )

    fun loadConfig(): ConfigState {
        return ConfigState(
            appName = prefs.getString("appName", "Elek ai") ?: "Elek ai",
            splashLogo = prefs.getString("splashLogo", "ic_sparkles") ?: "ic_sparkles",
            appLogo = prefs.getString("appLogo", "ic_sparkles") ?: "ic_sparkles",
            watermarkText = prefs.getString("watermarkText", "Elek") ?: "Elek",
            watermarkSize = prefs.getFloat("watermarkSize", 12f),
            watermarkPosition = prefs.getString("watermarkPosition", "Bottom Right") ?: "Bottom Right",
            watermarkAlpha = prefs.getFloat("watermarkAlpha", 0.5f),
            appLanguage = prefs.getString("appLanguage", "English") ?: "English",
            aiModel = prefs.getString("aiModel", "gemini-3.5-flash") ?: "gemini-3.5-flash",
            isPremium = prefs.getBoolean("isPremium", false),
            isAdsEnabled = prefs.getBoolean("isAdsEnabled", false),
            downloadQuality = prefs.getString("downloadQuality", "720p") ?: "720p",
            rewardTimeSec = prefs.getInt("rewardTimeSec", 20),
            isNotificationEnabled = prefs.getBoolean("isNotificationEnabled", true),
            isWelcomeEmailEnabled = prefs.getBoolean("isWelcomeEmailEnabled", true),
            apiKey = prefs.getString("apiKey", "") ?: "",
            isMaintenanceMode = prefs.getBoolean("isMaintenanceMode", false),
            analyticsEnabled = prefs.getBoolean("analyticsEnabled", true),
            feedbackEnabled = prefs.getBoolean("feedbackEnabled", true),
            serverStatus = prefs.getString("serverStatus", "Online / Healthy") ?: "Online / Healthy"
        )
    }

    fun updateConfig(update: (ConfigState) -> ConfigState) {
        val current = loadConfig()
        val next = update(current)
        
        prefs.edit().apply {
            putString("appName", next.appName)
            putString("splashLogo", next.splashLogo)
            putString("appLogo", next.appLogo)
            putString("watermarkText", next.watermarkText)
            putFloat("watermarkSize", next.watermarkSize)
            putString("watermarkPosition", next.watermarkPosition)
            putFloat("watermarkAlpha", next.watermarkAlpha)
            putString("appLanguage", next.appLanguage)
            putString("aiModel", next.aiModel)
            putBoolean("isPremium", next.isPremium)
            putBoolean("isAdsEnabled", next.isAdsEnabled)
            putString("downloadQuality", next.downloadQuality)
            putInt("rewardTimeSec", next.rewardTimeSec)
            putBoolean("isNotificationEnabled", next.isNotificationEnabled)
            putBoolean("isWelcomeEmailEnabled", next.isWelcomeEmailEnabled)
            putString("apiKey", next.apiKey)
            putBoolean("isMaintenanceMode", next.isMaintenanceMode)
            putBoolean("analyticsEnabled", next.analyticsEnabled)
            putBoolean("feedbackEnabled", next.feedbackEnabled)
            putString("serverStatus", next.serverStatus)
            apply()
        }
        
        _configFlow.value = next
    }
}
