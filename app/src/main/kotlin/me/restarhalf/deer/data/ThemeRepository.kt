package me.restarhalf.deer.data

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import top.yukonga.miuix.kmp.theme.ColorSchemeMode

data class ThemePreferences(
    val miuixColorSchemeMode: ColorSchemeMode = ColorSchemeMode.System,
    val miuixBottomBarFloating: Boolean = false,
    val backgroundAlpha: Float = 1f,
    val backgroundAmbiguity: Float = 0f,
    val componentsAlpha: Float = 1f,
    val backgroundImageUri: String? = null
)

object ThemeRepository {
    private const val PREFS_NAME = "theme_prefs"

    private const val KEY_MIUIX_COLOR_SCHEME_MODE = "miuix_color_scheme_mode"
    private const val KEY_MIUIX_BOTTOM_BAR_FLOATING = "miuix_bottom_bar_floating"
    private const val KEY_BACKGROUND_ALPHA = "background_alpha"
    private const val KEY_BACKGROUND_AMBIGUITY = "background_ambiguity"
    private const val KEY_BACKGROUND_IMAGE_URI = "background_image_uri"
    private const val KEY_COMPONENTS_ALPHA = "components_alpha"


    private val _themePreferences = MutableStateFlow(ThemePreferences())
    val themePreferences: StateFlow<ThemePreferences> = _themePreferences.asStateFlow()

    fun init(context: Context) {
        _themePreferences.value = load(context)
    }

    fun setMiuixColorSchemeMode(context: Context, mode: ColorSchemeMode) {
        update(context) { it.copy(miuixColorSchemeMode = mode) }
    }

    fun setMiuixBottomBarFloating(context: Context, floating: Boolean) {
        update(context) { it.copy(miuixBottomBarFloating = floating) }
    }

    fun setBackgroundAlpha(context: Context, alpha: Float) {
        update(context) { it.copy(backgroundAlpha = alpha.coerceIn(0f, 1f)) }
    }

    fun setBackgroundAmbiguity(context: Context, ambiguity: Float) {
        update(context) { it.copy(backgroundAmbiguity = ambiguity.coerceIn(0f, 1f)) }
    }

    fun setBackgroundImageUri(context: Context, uri: String?) {
        update(context) { it ->
            it.copy(
                backgroundImageUri = uri?.trim()?.takeIf { it.isNotBlank() })
        }
    }

    fun setComponentsAlpha(context: Context, alpha: Float) {
        update(context) { it.copy(componentsAlpha = alpha.coerceIn(0f, 1f)) }
    }

    private fun update(context: Context, block: (ThemePreferences) -> ThemePreferences) {
        val newValue = block(_themePreferences.value)
        _themePreferences.value = newValue
        save(context, newValue)
    }

    private fun load(context: Context): ThemePreferences {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val miuixModeStr = prefs.getString(KEY_MIUIX_COLOR_SCHEME_MODE, ColorSchemeMode.System.name)
        val miuixBottomBarFloating = prefs.getBoolean(KEY_MIUIX_BOTTOM_BAR_FLOATING, false)
        val backgroundAlpha = runCatching { prefs.getFloat(KEY_BACKGROUND_ALPHA, 1f) }
            .getOrDefault(1f)
            .coerceIn(0f, 1f)
        val backgroundAmbiguity = runCatching { prefs.getFloat(KEY_BACKGROUND_AMBIGUITY, 0f) }
            .getOrDefault(0f)
            .coerceIn(0f, 1f)
        val componentsAlpha = runCatching { prefs.getFloat(KEY_COMPONENTS_ALPHA, 1f) }
            .getOrDefault(1f)
            .coerceIn(0f, 1f)
        val backgroundImageUri = prefs.getString(KEY_BACKGROUND_IMAGE_URI, null)
            ?.trim()
            ?.takeIf { it.isNotBlank() }

        val miuixMode =
            runCatching { ColorSchemeMode.valueOf(miuixModeStr ?: ColorSchemeMode.System.name) }
                .getOrDefault(ColorSchemeMode.System)

        return ThemePreferences(
            miuixColorSchemeMode = miuixMode,
            miuixBottomBarFloating = miuixBottomBarFloating,
            backgroundAlpha = backgroundAlpha,
            backgroundAmbiguity = backgroundAmbiguity,
            backgroundImageUri = backgroundImageUri,
            componentsAlpha = componentsAlpha
        )
    }

    private fun save(context: Context, prefs: ThemePreferences) {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sp.edit {
            putString(KEY_MIUIX_COLOR_SCHEME_MODE, prefs.miuixColorSchemeMode.name)
            putBoolean(KEY_MIUIX_BOTTOM_BAR_FLOATING, prefs.miuixBottomBarFloating)
            putFloat(KEY_BACKGROUND_ALPHA, prefs.backgroundAlpha)
            putFloat(KEY_BACKGROUND_AMBIGUITY, prefs.backgroundAmbiguity)
            putString(KEY_BACKGROUND_IMAGE_URI, prefs.backgroundImageUri)
            putFloat(KEY_COMPONENTS_ALPHA, prefs.componentsAlpha)
        }
    }
}
