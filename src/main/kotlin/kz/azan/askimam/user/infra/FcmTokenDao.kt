package kz.azan.askimam.user.infra

import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository

interface FcmTokenDao : CrudRepository<FcmTokenRow, String> {
    fun findByUserId(id: Long): Set<FcmTokenRow>

    @Modifying
    @Query("delete from fcm_tokens where value = :value and user_id = :userId")
    fun deleteByValueAndUserId(value: String, userId: Long)
}
