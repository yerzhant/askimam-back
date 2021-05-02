package kz.azan.askimam.user.infra

import org.springframework.data.repository.CrudRepository

interface FcmTokenDao : CrudRepository<FcmTokenRow, String> {
    fun findByUserId(id: Long): Set<FcmTokenRow>
}
