package com.famas.tonz.core.core_states

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.famas.tonz.core.TAG
import com.famas.tonz.core.data.preferences.Preferences
import com.famas.tonz.feature_feed.data.remote.responses.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class UserDataState(
    private val preferences: Preferences,
    private val firebaseAuth: FirebaseAuth
) {
    private val _userData = mutableStateOf<User?>(null)
    val userData = _userData

    fun changeUserData(data: User) {
        _userData.value = data
    }

    fun clearUserData() {
        _userData.value = null
    }

    init {
        CoroutineScope(Dispatchers.Main).launch {
            preferences.getUserData()
                .collectLatest {
                    Log.d(TAG, "Current user: $it")
                    if (it == null) {
                        firebaseAuth.signOut()
                        return@collectLatest
                    }
                    _userData.value = it
                }
        }
    }
}