package me.restarhalf.deer

import android.app.Application
import me.restarhalf.deer.data.ThemeRepository
import me.restarhalf.deer.data.TimerRepository
import me.restarhalf.deer.ui.util.NotificationUtil

class DeerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ThemeRepository.init(this)
        TimerRepository.init(this)
        NotificationUtil.createChannel(this)
    }
}