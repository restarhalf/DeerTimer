package me.restarhalf.deer.ui.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.widget.ImageView
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import coil3.load
import coil3.request.allowHardware
import coil3.request.transformations
import coil3.size.Scale
import coil3.transform.RoundedCornersTransformation
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.config.SelectModeConfig
import com.luck.picture.lib.engine.ImageEngine
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.luck.picture.lib.utils.ActivityCompatHelper
import com.yalantis.ucrop.UCrop
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.io.File
import java.util.UUID

data class CropConfig(
    val aspectRatioX: Float = 0f,
    val aspectRatioY: Float = 0f,
    val maxWidth: Int = 1080,
    val maxHeight: Int = 1080,
    val compressionQuality: Int = 90,
    val freeStyleCrop: Boolean = true,
    val toolbarColor: Color = Color(0xFF333333),
    val statusBarColor: Color = Color(0xFF222222),
    val toolbarTitle: String = "裁切图片",
    val toolbarWidgetColor: Color = Color.White,
    val activeControlsWidgetColor: Color = Color(0xFF6200EE)
) {
    companion object {
        val Avatar = CropConfig(
            aspectRatioX = 1f,
            aspectRatioY = 1f,
            maxWidth = 512,
            maxHeight = 512,
            freeStyleCrop = false,
            toolbarTitle = "裁切头像"
        )

        val Background = CropConfig(
            aspectRatioX = 0f,
            aspectRatioY = 0f,
            maxWidth = 2048,
            maxHeight = 2048,
            freeStyleCrop = true,
            toolbarTitle = "裁切背景"
        )
    }
}

class ImageCropLauncher(
    private val context: Context,
    private val cropLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    private val cropConfig: CropConfig,
    private val onCropResult: (Uri?) -> Unit
) {
    fun launch() {
        val activity = context.findActivity()
        if (activity == null) {
            onCropResult(null)
            return
        }

        PictureSelector.create(activity)
            .openGallery(SelectMimeType.ofImage())
            .setSelectionMode(SelectModeConfig.SINGLE)
            .isDirectReturnSingle(true)
            .setImageEngine(PictureSelectorCoilEngine)
            .forResult(object : OnResultCallbackListener<LocalMedia> {
                override fun onResult(result: ArrayList<LocalMedia>) {
                    val uri = result.firstOrNull()?.toUri()
                    if (uri == null) {
                        onCropResult(null)
                        return
                    }
                    startCrop(uri)
                }

                override fun onCancel() {
                    onCropResult(null)
                }
            })
    }

    internal fun onCropFinished(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val resultUri = UCrop.getOutput(result.data!!)
            onCropResult(resultUri)
        } else {
            val error = result.data?.let { UCrop.getError(it) }
            error?.printStackTrace()
            onCropResult(null)
        }
    }

    private fun startCrop(sourceUri: Uri) {
        val destinationUri = createTempUri()

        val options = UCrop.Options().apply {
            setCompressionQuality(cropConfig.compressionQuality)
            setFreeStyleCropEnabled(cropConfig.freeStyleCrop)
            setToolbarColor(cropConfig.toolbarColor.toArgb())
            setStatusBarColor(cropConfig.statusBarColor.toArgb())
            setToolbarTitle(cropConfig.toolbarTitle)
            setToolbarWidgetColor(cropConfig.toolbarWidgetColor.toArgb())
            setActiveControlsWidgetColor(cropConfig.activeControlsWidgetColor.toArgb())
            setShowCropGrid(true)
            setShowCropFrame(true)
            setHideBottomControls(false)
        }

        val uCropIntent = UCrop.of(sourceUri, destinationUri)
            .withOptions(options)
            .apply {
                if (cropConfig.aspectRatioX > 0 && cropConfig.aspectRatioY > 0) {
                    withAspectRatio(cropConfig.aspectRatioX, cropConfig.aspectRatioY)
                }
                withMaxResultSize(cropConfig.maxWidth, cropConfig.maxHeight)
            }
            .getIntent(context)

        cropLauncher.launch(uCropIntent)
    }

    private fun createTempUri(): Uri {
        val cacheDir = File(context.cacheDir, "crop_cache")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        val tempFile = File(cacheDir, "crop_${UUID.randomUUID()}.jpg")
        return Uri.fromFile(tempFile)
    }
}

private object PictureSelectorCoilEngine : ImageEngine {
    override fun loadImage(context: Context, url: String, imageView: ImageView) {
        if (!ActivityCompatHelper.assertValidRequest(context)) {
            return
        }
        imageView.load(url)
    }

    override fun loadImage(
        context: Context,
        imageView: ImageView,
        url: String,
        maxWidth: Int,
        maxHeight: Int
    ) {
        if (!ActivityCompatHelper.assertValidRequest(context)) {
            return
        }
        imageView.load(url) {
            size(maxWidth, maxHeight)
        }
    }

    override fun loadAlbumCover(context: Context, url: String, imageView: ImageView) {
        if (!ActivityCompatHelper.assertValidRequest(context)) {
            return
        }
        imageView.load(url) {
            size(180, 180)
            scale(Scale.FILL)
            allowHardware(false)
            transformations(RoundedCornersTransformation(8f))
        }
    }

    override fun loadGridImage(context: Context, url: String, imageView: ImageView) {
        if (!ActivityCompatHelper.assertValidRequest(context)) {
            return
        }
        imageView.load(url) {
            size(200, 200)
            scale(Scale.FILL)
        }
    }

    override fun pauseRequests(context: Context) {
    }

    override fun resumeRequests(context: Context) {
    }
}

private fun LocalMedia.toUri(): Uri? {
    val mediaPath =
        availablePath
            ?.takeIf { it.isNotBlank() }
            ?: path?.takeIf { it.isNotBlank() }
            ?: return null

    return if (PictureMimeType.isContent(mediaPath)) {
        Uri.parse(mediaPath)
    } else {
        Uri.fromFile(File(mediaPath))
    }
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}

@Composable
fun rememberImageCropLauncher(
    cropConfig: CropConfig = CropConfig(),
    onResult: (Uri?) -> Unit
): ImageCropLauncher {
    val context = LocalContext.current

    val defaultConfig = remember { CropConfig() }
    val colorScheme = MiuixTheme.colorScheme
    val themedCropConfig = remember(cropConfig, colorScheme) {
        cropConfig.copy(
            toolbarColor = if (cropConfig.toolbarColor == defaultConfig.toolbarColor) {
                colorScheme.surface
            } else {
                cropConfig.toolbarColor
            },
            statusBarColor = if (cropConfig.statusBarColor == defaultConfig.statusBarColor) {
                colorScheme.surface
            } else {
                cropConfig.statusBarColor
            },
            toolbarWidgetColor = if (cropConfig.toolbarWidgetColor == defaultConfig.toolbarWidgetColor) {
                colorScheme.onSurface
            } else {
                cropConfig.toolbarWidgetColor
            },
            activeControlsWidgetColor =
                if (cropConfig.activeControlsWidgetColor == defaultConfig.activeControlsWidgetColor) {
                    colorScheme.primary
                } else {
                    cropConfig.activeControlsWidgetColor
                }
        )
    }

    lateinit var launcher: ImageCropLauncher

    val cropLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        launcher.onCropFinished(result)
    }

    launcher = remember(themedCropConfig, onResult) {
        ImageCropLauncher(
            context = context,
            cropLauncher = cropLauncher,
            cropConfig = themedCropConfig,
            onCropResult = onResult
        )
    }

    return launcher
}
