package com.safeguard.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.safeguard.data.local.entity.WhitelistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WhitelistDao {

    @Query("SELECT * FROM whitelist ORDER BY display_name ASC")
    fun getAllWhitelistFlow(): Flow<List<WhitelistEntity>>

    @Query("SELECT * FROM whitelist ORDER BY display_name ASC")
    suspend fun getAllWhitelist(): List<WhitelistEntity>

    @Query("SELECT * FROM whitelist WHERE id = :id") suspend fun getById(id: Long): WhitelistEntity?

    @Query("SELECT * FROM whitelist WHERE phone_number = :phoneNumber LIMIT 1")
    suspend fun getByPhoneNumber(phoneNumber: String): WhitelistEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM whitelist WHERE phone_number = :phoneNumber)")
    suspend fun isWhitelisted(phoneNumber: String): Boolean

    @Query(
            "SELECT EXISTS(SELECT 1 FROM whitelist WHERE phone_number = :phoneNumber AND allow_calls = 1)"
    )
    suspend fun isCallAllowed(phoneNumber: String): Boolean

    @Query(
            "SELECT EXISTS(SELECT 1 FROM whitelist WHERE phone_number = :phoneNumber AND allow_sms = 1)"
    )
    suspend fun isSmsAllowed(phoneNumber: String): Boolean

    @Query("SELECT COUNT(*) FROM whitelist") fun getCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM whitelist") suspend fun getCount(): Int

    @Query(
            "SELECT * FROM whitelist WHERE display_name LIKE '%' || :query || '%' OR phone_number LIKE '%' || :query || '%'"
    )
    fun searchWhitelist(query: String): Flow<List<WhitelistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: WhitelistEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<WhitelistEntity>)

    @Update suspend fun update(entity: WhitelistEntity)

    @Delete suspend fun delete(entity: WhitelistEntity)

    @Query("DELETE FROM whitelist WHERE id = :id") suspend fun deleteById(id: Long)

    @Query("DELETE FROM whitelist WHERE phone_number = :phoneNumber")
    suspend fun deleteByPhoneNumber(phoneNumber: String)

    @Query("DELETE FROM whitelist") suspend fun deleteAll()
}
