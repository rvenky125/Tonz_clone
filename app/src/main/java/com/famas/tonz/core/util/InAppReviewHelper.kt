package com.famas.tonz.core.util

import android.app.Activity
import android.app.Dialog
import android.preference.DialogPreference
import android.util.Log
import androidx.core.app.DialogCompat
import com.famas.tonz.core.data.preferences.Preferences
import com.google.android.play.core.review.ReviewManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InAppReviewHelper(
    private val reviewManager: ReviewManager,
    private val preferences: Preferences
) {
    private lateinit var activity: Activity

    fun initialize(activity: Activity) {
        this.activity = activity
    }

    suspend fun askForReview() {
        withContext(Dispatchers.IO) {
            if (!this@InAppReviewHelper::activity.isInitialized) {
                return@withContext
            }
            val currentCount = preferences.getUpdateDialogToShowCount().firstOrNull() ?: 1
            preferences.incrementUpdateDialogToShowCount()

            if (currentCount % 3 == 0) {
                return@withContext
            }

            reviewManager.requestReviewFlow().addOnSuccessListener {
                CoroutineScope(Dispatchers.Main).launch {
                    reviewManager.launchReviewFlow(activity, it).addOnSuccessListener {
                    }
                }
            }
        }
    }
}