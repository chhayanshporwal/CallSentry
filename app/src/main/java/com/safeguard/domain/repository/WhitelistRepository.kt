package com.safeguard.domain.repository

import com.safeguard.domain.model.WhitelistContact
import kotlinx.coroutines.flow.Flow

interface WhitelistRepository {

    fun getAllContacts(): Flow<List<WhitelistContact>>

    fun getContactCount(): Flow<Int>

    fun searchContacts(query: String): Flow<List<WhitelistContact>>

    suspend fun getContactById(id: Long): WhitelistContact?

    suspend fun getContactByPhoneNumber(phoneNumber: String): WhitelistContact?

    suspend fun isWhitelisted(phoneNumber: String): Boolean

    suspend fun isCallAllowed(phoneNumber: String): Boolean

    suspend fun isSmsAllowed(phoneNumber: String): Boolean

    suspend fun addContact(contact: WhitelistContact): Long

    suspend fun addContacts(contacts: List<WhitelistContact>)

    suspend fun updateContact(contact: WhitelistContact)

    suspend fun removeContact(contact: WhitelistContact)

    suspend fun removeContactById(id: Long)

    suspend fun clearAll()
}
