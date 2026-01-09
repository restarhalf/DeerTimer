package me.restarhalf.deer.ui.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.restarhalf.deer.MainActivity
import me.restarhalf.deer.R
import me.restarhalf.deer.ui.util.NotificationUtil
import me.restarhalf.deer.ui.util.formatTime


class TimerService : Service() {
    private val binder = LocalBinder()
    private val _elapsedSec = MutableStateFlow(0)
    val elapsedSec: StateFlow<Int> = _elapsedSec.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private var startTimeMs: Long = 0L
    private var accumulatedSec: Int = 0

    private val handler = Handler(Looper.getMainLooper())
    private val tickRunnable = object : Runnable {
        @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
        override fun run() {
            val nowMs = SystemClock.elapsedRealtime()
            _elapsedSec.value = accumulatedSec + ((nowMs - startTimeMs) / 1000).toInt()
            updateNotification(_elapsedSec.value)
            handler.postDelayed(this, 1000)
        }
    }

    override fun onBind(intent: Intent): IBinder = binder

    inner class LocalBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    @SuppressLint("ForegroundServiceType")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startTimer()
            ACTION_PAUSE -> pauseTimer()
            ACTION_STOP -> stopTimer()
        }
        return START_STICKY
    }

    private fun startTimer() {
        if (startTimeMs == 0L) {
            startTimeMs = SystemClock.elapsedRealtime()
        }
        _isRunning.value = true
        handler.removeCallbacks(tickRunnable)
        handler.post(tickRunnable)
        val notif = buildNotification(_elapsedSec.value)
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIF_ID, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
            } else {
                startForeground(NOTIF_ID, notif)
            }
        }.onFailure {
            handler.removeCallbacks(tickRunnable)
            _isRunning.value = false
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun pauseTimer() {
        handler.removeCallbacks(tickRunnable)
        accumulatedSec = _elapsedSec.value
        startTimeMs = 0L
        _isRunning.value = false
        runCatching { stopForegroundCompat(removeNotification = false) }
        updateNotification(_elapsedSec.value)
    }

    private fun stopTimer() {
        handler.removeCallbacks(tickRunnable)
        accumulatedSec = 0
        startTimeMs = 0L
        _elapsedSec.value = 0
        _isRunning.value = false
        runCatching { stopForegroundCompat(removeNotification = true) }
        runCatching { NotificationManagerCompat.from(this).cancel(NOTIF_ID) }
        stopSelf()
    }

    private fun buildNotification(elapsed: Int): Notification {
        val contentText = formatTime(elapsed)

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val openAppPendingIntent = PendingIntent.getActivity(this, 0, openAppIntent, flags)

        return NotificationCompat.Builder(this, NotificationUtil.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(if (_isRunning.value) "计时进行中" else "计时已暂停")
            .setContentText(contentText)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(_isRunning.value)
            .setOnlyAlertOnce(true)
            .build()
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun updateNotification(elapsed: Int) {
        val notif = buildNotification(elapsed)
        runCatching {
            NotificationManagerCompat.from(this).notify(NOTIF_ID, notif)
        }
    }

    private fun stopForegroundCompat(removeNotification: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            stopForeground(if (removeNotification) STOP_FOREGROUND_REMOVE else STOP_FOREGROUND_DETACH)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(removeNotification)
        }
    }

    override fun onDestroy() {
        handler.removeCallbacks(tickRunnable)
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "me.restarhalf.deer.ACTION_START"
        const val ACTION_PAUSE = "me.restarhalf.deer.ACTION_PAUSE"
        const val ACTION_STOP = "me.restarhalf.deer.ACTION_STOP"
        const val NOTIF_ID = 1001

        fun startIntent(context: Context): Intent =
            Intent(context, TimerService::class.java).apply {
                action = ACTION_START
            }

        fun pauseIntent(context: Context): Intent =
            Intent(context, TimerService::class.java).apply {
                action = ACTION_PAUSE
            }

        fun stopIntent(context: Context): Intent = Intent(context, TimerService::class.java).apply {
            action = ACTION_STOP
        }
    }
}
