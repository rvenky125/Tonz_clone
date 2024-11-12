package com.famas.tonz.feature_trim_set_ringtone.presentation

sealed class TrimRingtoneEvent {
    object OnZoomIn: TrimRingtoneEvent()
    object OnZoomOut: TrimRingtoneEvent()
    object SkipPrevious : TrimRingtoneEvent()
    object OnSetRingtone : TrimRingtoneEvent()
    object OnSaveRingtone: TrimRingtoneEvent()
    data class SetShowShareRingtone(val value: Boolean) : TrimRingtoneEvent()
    data class OnDragHandle(val handle: TrimHandle, val x: Float, val width: Float?): TrimRingtoneEvent()
    data class OnLeftHandleEvent(val event: HandleEvent): TrimRingtoneEvent()
    data class OnRightHandleEvent(val event: HandleEvent): TrimRingtoneEvent()
    data class OnToggleShare(val value: Boolean): TrimRingtoneEvent()
    object ShowGoBackDialog: TrimRingtoneEvent()
    object DismissGoBackDialog: TrimRingtoneEvent()
}


enum class HandleEvent {
    Decrease,
    Increase
}