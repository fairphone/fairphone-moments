/*
 * Copyright (C) 2026 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.data.datasource

import android.app.Application
import android.provider.ContactsContract
import android.util.Log
import androidx.core.database.getStringOrNull
import com.fairphone.spring.launcher.data.model.ContactInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface DeviceContactDataSource {
    suspend fun getDeviceContacts(): List<ContactInfo>
    suspend fun getContactsPhones(contactIds: List<String>): Map<String, List<String>>
}

class DeviceContactDataSourceImpl(
    private val appContext: Application,
) : DeviceContactDataSource {

    companion object {
        val CONTACT_PROJECTION: Array<out String> = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
            ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
            ContactsContract.Contacts.PHOTO_URI,
            ContactsContract.Contacts.LOOKUP_KEY,
        )
    }

    override suspend fun getDeviceContacts(): List<ContactInfo> = withContext(Dispatchers.IO) {
        val contactsList = mutableListOf<ContactInfo>()

        appContext.contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            CONTACT_PROJECTION,
            null, // Selection (WHERE clause)
            null, // Selection arguments
            ContactsContract.Contacts.DISPLAY_NAME + " ASC" // Sort order
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID)
            val nameColumn =
                cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
            val lookupKeyColumn = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.LOOKUP_KEY)
            val photoThumbnailUriColumn =
                cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI)
            val photoUriColumn = cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI)

            while (cursor.moveToNext()) {
                try {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getStringOrNull(nameColumn) ?: "Unknown"
                    val lookupKey = cursor.getString(lookupKeyColumn)
                    val photoUri = cursor.getStringOrNull(photoThumbnailUriColumn)
                        ?: cursor.getStringOrNull(photoUriColumn)

                    val lookupUri = ContactsContract.Contacts.getLookupUri(id, lookupKey)

                    val contactInfo = ContactInfo(
                        id = id.toString(),
                        name = name,
                        icon = photoUri,
                        contactUri = lookupUri,
                    )

                    Log.d("ContactRepo", "Found contact: $contactInfo")

                    contactsList.add(contactInfo)
                } catch (e: Exception) {
                    Log.e("ContactsRepo", "Failed to query contact", e)
                }

            }
        }
        contactsList
    }

    override suspend fun getContactsPhones(contactIds: List<String>): Map<String, List<String>> {
        return withContext(Dispatchers.IO) {
            Log.d("ContactRepo", "Fetching phones for contacts: $contactIds")

            val contactPhoneNumbersMap = mutableMapOf<String, MutableList<String>>()

            val phoneProjection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            )

            val selection: String?
            val selectionArgs: Array<String>?

            // If contactIds are provided and not empty, build a selection clause to filter results
            if (contactIds.isNotEmpty()) {
                val placeholders = contactIds.joinToString(separator = ",") { "?" }
                selection = "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} IN ($placeholders)"
                selectionArgs = contactIds.toTypedArray()
            } else {
                // No filtering needed, fetch all phone numbers
                selection = null
                selectionArgs = null
            }

            appContext.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                phoneProjection,
                selection, // Apply the selection filter if contactIds were provided
                selectionArgs, // Apply selection arguments
                null  // No sorting
            )?.use { phoneCursor ->
                val contactIdColumn = phoneCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val numberColumn = phoneCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)

                while (phoneCursor.moveToNext()) {
                    val contactId = phoneCursor.getString(contactIdColumn)
                    val phoneNumber = phoneCursor.getString(numberColumn)

                    Log.d("ContactRepo", "Found phone number for contact $contactId: $phoneNumber")

                    if (contactId != null && phoneNumber != null) {
                        contactPhoneNumbersMap
                            .getOrPut(contactId) { mutableListOf() }
                            .add(refinePhoneNumber(phoneNumber))
                    }
                }
            }
            contactPhoneNumbersMap
        }
    }

    private fun refinePhoneNumber(phoneNumber: String): String {
        return phoneNumber.replace("\\s".toRegex(), "")
    }
}