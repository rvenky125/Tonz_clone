package com.famas.tonz.core.util

import android.content.ContentValues
import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.provider.MediaStore
import android.util.Log
import com.famas.tonz.core.TAG
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException


class RingtoneApi(private val context: Context) {
    fun setCustomRingtone(ringtoneUri: String, phoneNumber: String): Boolean {
        try {
            val lookupUri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI, phoneNumber
            )

            // The columns used for `Contacts.getLookupUri`
            val projection = arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.LOOKUP_KEY,
                ContactsContract.Contacts.DISPLAY_NAME,
            )
            val data = context.contentResolver.query(lookupUri, projection, null, null, null)
            if (data != null && data.moveToFirst()) {
                data.moveToFirst()
                // Get the contact lookup Uri
                val contactId = data.getLong(0)
                val lookupKey = data.getString(1)
                val name = data.getString(2)

                setCustomRingtone(
                    lookupKey = lookupKey, contactId = contactId, ringtoneUri = ringtoneUri
                )
            }
            data?.close()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    private fun setCustomRingtone(lookupKey: String, contactId: Long, ringtoneUri: String) {
        Log.d(TAG, "setting custom ringtone: $ringtoneUri")
        val values = ContentValues()
        val contactUri = ContactsContract.Contacts.getLookupUri(contactId, lookupKey)
        values.put(ContactsContract.Contacts.CUSTOM_RINGTONE, ringtoneUri)
        context.contentResolver.update(contactUri, values, null, null)
    }

    fun setDefaultRingtone(file: File): Boolean {
        return try {
            val uri = saveFileAsRingtoneAndGetUri(file)
            RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE, uri)
//            Toast.makeText(context, "Ringtone set successfully", Toast.LENGTH_LONG).show()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun setCustomRingtone(file: File, lookupKey: String, contactId: Long): Boolean {
        val uri = saveFileAsRingtoneAndGetUri(file)

        return try {
            uri?.let {
                setCustomRingtone(
                    lookupKey = lookupKey,
                    contactId = contactId,
                    ringtoneUri = it.toString()
                )
                true
            } ?: run {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun setCustomRingtoneToContacts(file: File, contacts: List<ContactIds>): Boolean {
        val uri = saveFileAsRingtoneAndGetUri(file)
        Log.d(TAG, "returned uri of saved file: $uri")

        return try {
            uri?.let { safeUri ->
                contacts.forEach {
                    Log.d(TAG, "Setting ringtone to: $it")
                    setCustomRingtone(
                        lookupKey = it.lookupKey,
                        contactId = it.contactId,
                        ringtoneUri = safeUri.toString()
                    )
                }
                true
            } ?: run {
                false
            }
        } catch (e: Exception) {
            Log.d(TAG, "failed to set ringtone to contacts", e)
            false
        }
    }

    private fun saveFileAsRingtoneAndGetUri(file: File): Uri? {
        val values = ContentValues()
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
        values.put(
            MediaStore.MediaColumns.TITLE,
            file.name.substringBeforeLast(".")
        )
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mpeg")
        values.put(MediaStore.Audio.Media.IS_RINGTONE, true)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val newUri: Uri =
                context.contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
                    ?: return null
            try {
                context.contentResolver.openOutputStream(newUri).use { os ->
                    val size = file.length().toInt()
                    val bytes = ByteArray(size)
                    try {
                        val buf = BufferedInputStream(FileInputStream(file))
                        buf.read(bytes, 0, bytes.size)
                        buf.close()
                        os?.write(bytes)
                        os?.close()
                        os?.flush()
                    } catch (e: IOException) {
                        return null
                    }
                }
            } catch (ignored: Exception) {
                ignored.printStackTrace()
                return null
            }
            newUri
        } else {
            values.put(MediaStore.MediaColumns.DATA, file.absolutePath)
            val uri = MediaStore.Audio.Media.getContentUriForPath(file.absolutePath)
            Log.d(TAG, "media store uri: $uri")
            uri?.let {
                context.contentResolver.delete(
                    it, MediaStore.MediaColumns.DATA + "=\"" + file.absolutePath + "\"", null
                )
            } ?: return null
            val newUri: Uri? = context.contentResolver.insert(uri, values)
//            MediaStore.Audio.Media.getContentUriForPath(
//                file.absolutePath
//            )?.let {
//                val insertedUri = context.contentResolver.insert(it, values)
//                Log.d(TAG, "media store inserted content uri: $insertedUri $values")
//                insertedUri
//            } ?: return null
            newUri
        }
    }

    fun setCustomRingtone(file: File, phoneNumber: String): Boolean {
        return try {
            Log.d(TAG, "file path in app: ${file.absolutePath}")
            val lookupUri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI, phoneNumber
            )

            // The columns used for `Contacts.getLookupUri`
            val projection = arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.LOOKUP_KEY,
                ContactsContract.Contacts.DISPLAY_NAME,
            )
            val data = context.contentResolver.query(lookupUri, projection, null, null, null)
            if (data != null && data.moveToFirst()) {
                data.moveToFirst()
                // Get the contact lookup Uri
                val contactId = data.getLong(0)
                val lookupKey = data.getString(1)
                val displayName = data.getString(2)

                val values = ContentValues()
                values.put(MediaStore.MediaColumns.DATA, file.absolutePath)
                values.put(MediaStore.MediaColumns.TITLE, file.name)
                values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/ogg")
                values.put(MediaStore.Audio.Media.IS_RINGTONE, true)

                val newUri = context.contentResolver.insert(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values
                )

                Log.d(TAG, "newUri $newUri")

                if (newUri != null) {
                    val contactUri = ContactsContract.Contacts.getLookupUri(contactId, lookupKey)
                    values.put(ContactsContract.Contacts.CUSTOM_RINGTONE, newUri.toString())
                    context.contentResolver.update(contactUri, values, null, null)
                }
                data.close()
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getContactIdByLookupKey(context: Context, lookupKey: String): Long? {
        try {
            val uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey)
            val projection = arrayOf(ContactsContract.Contacts._ID)
            val cursor = context.contentResolver.query(uri, projection, null, null, null)

            return if (cursor != null && cursor.moveToFirst()) {
                val contactId =
                    cursor.getLong(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                cursor.close()
                contactId
            } else {
                cursor?.close()
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun getContactIdByPhoneNumber(context: Context, phoneNumber: String): Long? {
        try {
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber)
            )
            val projection = arrayOf(ContactsContract.PhoneLookup._ID)
            val cursor = context.contentResolver.query(uri, projection, null, null, null)

            return if (cursor != null && cursor.moveToFirst()) {
                val contactId =
                    cursor.getLong(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID))
                cursor.close()
                contactId
            } else {
                Log.d("myTag", "closing it $cursor ${cursor?.moveToFirst()}")
                cursor?.close()
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}

data class ContactIds(
    val lookupKey: String,
    val contactId: Long
)