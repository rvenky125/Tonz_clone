package com.famas.tonz.core.ui.util

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.famas.tonz.core.ui.navigation.Screen

sealed class UiEvent {
    data class ShowSnackBar(val uiText: UiText): UiEvent()
    data class OnNavigate(val screen: Screen): UiEvent()
    object NavigateBack: UiEvent()
}


sealed class UiText {
    data class StringResource(@StringRes val id: Int, val args: List<Any> = emptyList()): UiText()
    data class DynamicString(val value: String): UiText()

    fun getString(context: Context) = when(this) {
        is DynamicString -> string()
        is StringResource -> string(context)
    }

    fun StringResource.string(context: Context) = context.getString(id, *args.toTypedArray())
    fun DynamicString.string() = value

    @Composable
    fun StringResource.asString(): String {
        return stringResource(id = id)
    }
}