package com.famas.tonz.core.ui.navigation

import com.famas.tonz.core.util.UiContact
import kotlinx.serialization.Serializable

sealed class UiEventNavArgs

@Serializable
data class TrimRingtoneNavArgs(
    val contactsArg: ContactsNavArg,
    val contentId: String? = null,
    val songUri: String? = null,
) : UiEventNavArgs()

@Serializable
data class ContactsNavArg(
    val contacts: List<UiContact> = listOf(),
)

@Serializable
data class AudioListScreenNavArgs(
    val contacts: List<UiContact>? = null
) : UiEventNavArgs()