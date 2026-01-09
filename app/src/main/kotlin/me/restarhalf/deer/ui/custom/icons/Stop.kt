package me.restarhalf.deer.ui.custom.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.icon.MiuixIcons

val MiuixIcons.Stop: ImageVector
    get() {
        if (_stop != null) return _stop!!
        _stop = ImageVector.Builder(
            name = "Stop",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(8f, 6f)
                lineTo(16f, 6f)
                quadTo(18f, 6f, 18f, 8f)
                lineTo(18f, 16f)
                quadTo(18f, 18f, 16f, 18f)
                lineTo(8f, 18f)
                quadTo(6f, 18f, 6f, 16f)
                lineTo(6f, 8f)
                quadTo(6f, 6f, 8f, 6f)
                close()
            }
        }.build()
        return _stop!!
    }

private var _stop: ImageVector? = null
