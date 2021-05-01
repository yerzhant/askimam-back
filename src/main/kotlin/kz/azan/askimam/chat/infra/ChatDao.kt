package kz.azan.askimam.chat.infra

import kz.azan.askimam.chat.domain.model.Chat
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
interface ChatDao : CrudRepository<ChatRow, Long> {
    fun findByTypeAndIsVisibleToPublicIsTrueOrderByUpdatedAtDesc(type: Chat.Type, pageable: Pageable): List<ChatRow>
    fun findByAnsweredByOrderByUpdatedAtDesc(userId: Long, pageable: Pageable): List<ChatRow>
    fun findByAskedByOrderByUpdatedAtDesc(userId: Long, pageable: Pageable): List<ChatRow>
    fun findByAnsweredByIsNullOrderByUpdatedAtDesc(pageable: Pageable): List<ChatRow>
}
