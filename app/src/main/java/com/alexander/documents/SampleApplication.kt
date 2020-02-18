package com.alexander.documents

import android.app.Application
import com.alexander.documents.ui.MainActivity
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKTokenExpiredHandler

/**
 * author alex
 */
class SampleApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        VK.addTokenExpiredHandler(tokenTracker)
    }

    private val tokenTracker = object: VKTokenExpiredHandler {
        override fun onTokenExpired() {
            MainActivity.createIntent(this@SampleApplication)
        }
    }
}