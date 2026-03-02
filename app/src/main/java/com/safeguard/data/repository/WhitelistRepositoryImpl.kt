package com.safeguard.data.repository

import com.safeguard.data.local.dao.WhitelistDao
import com.safeguard.data.local.entity.WhitelistEntity
import com.safeguard.domain.model.WhitelistContact
import com.safeguard.domain.repository.WhitelistRepository
import com.safeguard.util.PhoneNumberUtils
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class WhitelistRepositoryImpl @Inject constructor(private val whitelistDao: WhitelistDao) :
        WhitelistRepository {

    override fun getAllContacts(): Flow<List<WhitelistContact>> {
        return whitelistDao.getAllWhitelistFlow().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getContactCount(): Flow<Int> {
        return whitelistDao.getCountFlow()
    }

    override fun searchContacts(query: String): Flow<List<WhitelistContact>> {
        return whitelistDao.searchWhitelist(query).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getContactById(id: Long): WhitelistContact? {
        return whitelistDao.getById(id)?.toDomainModel()
    }

    override suspend fun getContactByPhoneNumber(phoneNumber: String): WhitelistContact? {
        val normalized = PhoneNumberUtils.normalize(phoneNumber)
        return whitelistDao.getByPhoneNumber(normalized)?.toDomainModel()
    }

    override suspend fun isWhitelisted(phoneNumber: String): Boolean {
        val normalized = PhoneNumberUtils.normalize(phoneNumber)
        // Also check alternative formats
        val alternatives = PhoneNumberUtils.getAlternativeFormats(normalized)
        return alternatives.any { whitelistDao.isWhitelisted(it) }
    }

    override suspend fun isCallAllowed(phoneNumber: String): Boolean {
        val normalized = PhoneNumberUtils.normalize(phoneNumber)
        val alternatives = PhoneNumberUtils.getAlternativeFormats(normalized)
        return alternatives.any { whitelistDao.isCallAllowed(it) }
    }

    override suspend fun isSmsAllowed(phoneNumber: String): Boolean {
        val normalized = PhoneNumberUtils.normalize(phoneNumber)
        val alternatives = PhoneNumberUtils.getAlternativeFormats(normalized)
        return alternatives.any { whitelistDao.isSmsAllowed(it) }
    }

    override suspend fun addContact(contact: WhitelistContact): Long {
        return whitelistDao.insert(contact.toEntity())
    }

    override suspend fun addContacts(contacts: List<WhitelistContact>) {
        whitelistDao.insertAll(contacts.map { it.toEntity() })
    }

    override suspend fun updateContact(contact: WhitelistContact) {
        whitelistDao.update(contact.toEntity())
    }

    override suspend fun removeContact(contact: WhitelistContact) {
        whitelistDao.delete(contact.toEntity())
    }

    override suspend fun removeContactById(id: Long) {
        whitelistDao.deleteById(id)
    }

    override suspend fun clearAll() {
        whitelistDao.deleteAll()
    }

    // Mappers
    private fun WhitelistEntity.toDomainModel(): WhitelistContact {
        return WhitelistContact(
                id = id,
                phoneNumber = phoneNumber,
                displayName = displayName,
                contactUri = contactUri,
                allowCalls = allowCalls,
                allowSms = allowSms,
                createdAt = createdAt
        )
    }

    private fun WhitelistContact.toEntity(): WhitelistEntity {
        return WhitelistEntity(
                id = id,
                phoneNumber = PhoneNumberUtils.normalize(phoneNumber),
                displayName = displayName,
                contactUri = contactUri,
                allowCalls = allowCalls,
                allowSms = allowSms,
                createdAt = createdAt,
                updatedAt = System.currentTimeMillis()
        )
    }
}
