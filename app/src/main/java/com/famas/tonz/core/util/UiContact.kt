package com.famas.tonz.core.util

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.ContactsContract
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import contacts.core.Contacts
import contacts.core.entities.Contact
import contacts.core.equalTo
import contacts.core.util.phoneList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.io.InputStream


@Serializable
data class UiContact(
    val name: String,
    val profilePicUri: String?,
    val currentRingtoneUri: String?,
    val phoneNumbers: List<String?>,
    val contactId: Long,
    val lookupKey: String,
    val currentRingtoneName: String?,
)

fun UiContact.toDataContact(context: Context): Contact? {
    val contacts = Contacts(context)
    return contacts.query().where {
        Contact.Id equalTo this@toDataContact.contactId
    }.find().firstOrNull()
}

fun Contact.toUiContact(): UiContact? {
    return UiContact(
        name = displayNamePrimary ?: if (hasPhoneNumber == true) {
            phoneList().first().number
                ?: return null
        } else return null,
        profilePicUri = this.photoUri?.toString(),
        contactId = id,
        currentRingtoneUri = options?.customRingtone?.toString(),
        lookupKey = lookupKey ?: return null,
        phoneNumbers = phoneList().mapNotNull { it.number }.takeIf { it.isNotEmpty() }
            ?: return null,
        currentRingtoneName = null
    )
}

fun openInputStreamForPhotoOfContact(context: Context, contactId: Long): InputStream =
    ContactsContract.Contacts.openContactPhotoInputStream(
        context.contentResolver,
        ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)
    )

@Composable
fun rememberBitmapForContact(contactId: Long): Bitmap? {
    val context = LocalContext.current
    val bitmap = remember { mutableStateOf<Bitmap?>(null) }
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(key1 = contactId) {
        val contactImageJob = coroutineScope.launch {
            if (bitmap.value != null) {
                return@launch
            }
            delay(500)
            try {
                openInputStreamForPhotoOfContact(context, contactId).use {
                    bitmap.value = BitmapFactory.decodeStream(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        onDispose {
            contactImageJob.cancel()
        }
    }

    return bitmap.value
}

fun String.toOneOrTwoLetters(): String {
    return split(" ")
        .map { first() }
        .subList(0, 1)
        .joinToString()
}