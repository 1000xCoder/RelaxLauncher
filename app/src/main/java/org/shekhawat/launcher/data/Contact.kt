package org.shekhawat.launcher.data

import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract

data class Contact(val name: String, val phoneNumber: String)

fun fetchContacts(context: Context): List<Contact> {
    val contacts = mutableListOf<Contact>()
    val cursor: Cursor? = context.contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        ),
        null,
        null,
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
    )

    cursor?.use {
        val nameIndex = it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
        val numberIndex = it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)

        while (it.moveToNext()) {
            val name = it.getString(nameIndex) ?: continue
            val phoneNumber = it.getString(numberIndex) ?: continue
            contacts.add(Contact(name, phoneNumber))
        }
    }

    return contacts
}
