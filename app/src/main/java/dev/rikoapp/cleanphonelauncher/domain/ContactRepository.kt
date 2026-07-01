package dev.rikoapp.cleanphonelauncher.domain

import dev.rikoapp.cleanphonelauncher.domain.model.Contact

interface ContactRepository {
    fun hasPermission(): Boolean
    fun search(query: String): List<Contact>
    fun openContact(contact: Contact)
}
