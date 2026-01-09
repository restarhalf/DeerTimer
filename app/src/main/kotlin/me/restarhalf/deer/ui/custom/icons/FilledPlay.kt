package me.restarhalf.deer.ui.custom.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathNode
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.icon.MiuixIcons

val MiuixIcons.FilledPlay: ImageVector
    get() {
        if (_play != null) return _play!!
        _play = ImageVector.Builder(
            name = "Play",
            defaultWidth = 24.0f.dp,
            defaultHeight = 24.0f.dp,
            viewportWidth = 1162.9090909090908f,
            viewportHeight = 1162.9090909090908f,
        ).apply {
            group(
                scaleX = 1.0f,
                scaleY = -1.0f,
                translationX = -104.04545454545462f,
                translationY = 956.4545454545454f
            ) {
                addPath(
                    pathData = listOf(
                        PathNode.MoveTo(521.0f, -48.0f),
                        PathNode.LineTo(980.0f, 218.0f),
                        PathNode.QuadTo(1046.0f, 256.0f, 1073.0f, 276.0f),
                        PathNode.QuadTo(1100.0f, 296.0f, 1112.0f, 323.0f),
                        PathNode.QuadTo(1123.0f, 348.0f, 1123.0f, 376.0f),
                        PathNode.QuadTo(1123.0f, 404.0f, 1112.0f, 429.0f),
                        PathNode.QuadTo(1100.0f, 455.0f, 1073.0f, 475.5f),
                        PathNode.QuadTo(1046.0f, 496.0f, 980.0f, 533.0f),
                        PathNode.LineTo(521.0f, 799.0f),
                        PathNode.QuadTo(459.0f, 835.0f, 426.0f, 848.5f),
                        PathNode.QuadTo(393.0f, 862.0f, 365.0f, 859.0f),
                        PathNode.QuadTo(338.0f, 857.0f, 314.0f, 843.5f),
                        PathNode.QuadTo(290.0f, 830.0f, 273.0f, 807.0f),
                        PathNode.QuadTo(256.0f, 785.0f, 252.0f, 751.5f),
                        PathNode.QuadTo(248.0f, 718.0f, 248.0f, 642.0f),
                        PathNode.VerticalTo(111.0f),
                        PathNode.QuadTo(248.0f, 33.0f, 251.5f, 0.0f),
                        PathNode.QuadTo(255.0f, -33.0f, 272.0f, -57.0f),
                        PathNode.QuadTo(289.0f, -79.0f, 313.5f, -93.0f),
                        PathNode.QuadTo(338.0f, -107.0f, 364.0f, -109.0f),
                        PathNode.QuadTo(393.0f, -112.0f, 425.5f, -98.5f),
                        PathNode.QuadTo(458.0f, -85.0f, 521.0f, -48.0f),
                        PathNode.Close,
                    ),
                    fill = SolidColor(Color.Black),
                    fillAlpha = 1f,
                    pathFillType = PathFillType.NonZero,
                )
            }
        }.build()
        return _play!!
    }

private var _play: ImageVector? = null
