package dev.rikoapp.cleanphonelauncher.data

import android.Manifest
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import dev.rikoapp.cleanphonelauncher.domain.ContactRepository
import dev.rikoapp.cleanphonelauncher.domain.model.Contact

class AndroidContactRepository(
    private val context: Application
) : ContactRepository {

    override fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) ==
            PackageManager.PERMISSION_GRANTED

    override fun search(query: String): List<Contact> {
        if (query.isBlank() || !hasPermission()) return emptyList()

        val uri = Uri.withAppendedPath(
            ContactsContract.Contacts.CONTENT_FILTER_URI,
            Uri.encode(query)
        )
        val projection = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.LOOKUP_KEY,
            ContactsContract.Contacts.DISPLAY_NAME
        )

        val results = mutableListOf<Contact>()
        val seen = mutableSetOf<Long>()
        runCatching {
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                val idIndex = cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID)
                val lookupIndex = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.LOOKUP_KEY)
                val nameIndex = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)
                while (cursor.moveToNext() && results.size < 5) {
                    val id = cursor.getLong(idIndex)
                    val name = cursor.getString(nameIndex)?.takeIf { it.isNotBlank() } ?: continue
                    val lookupKey = cursor.getString(lookupIndex) ?: continue
                    if (seen.add(id)) {
                        results.add(Contact(id = id, lookupKey = lookupKey, name = name))
                    }
                }
            }
        }
        return results
    }

    override fun openContact(contact: Contact) {
        val uri = ContactsContract.Contacts.getLookupUri(contact.id, contact.lookupKey)
        val intent = Intent(Intent.ACTION_VIEW, uri)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }
}
