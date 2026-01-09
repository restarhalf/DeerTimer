package me.restarhalf.deer.ui.util

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImage
import coil3.request.ImageRequest

@Composable
fun AvatarCircle(
    avatarUrl: String?,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Transparent,
    contentDescription: String? = null,
    fallback: @Composable () -> Unit
) {
    val cleaned = avatarUrl
        ?.trim()
        ?.takeIf { it.isNotBlank() }

    val context = LocalContext.current.applicationContext
    val request = remember(cleaned) {
        cleaned?.let {
            ImageRequest.Builder(context)
                .data(it)
                .size(192, 192)
                .diskCacheKey(it)
                .build()
        }
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        fallback()
        AsyncImage(
            model = request,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}
