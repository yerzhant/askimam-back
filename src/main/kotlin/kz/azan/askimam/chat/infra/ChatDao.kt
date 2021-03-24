package kz.azan.askimam.chat.infra

import kz.azan.askimam.chat.domain.model.Chat
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository

interface ChatDao : CrudRepository<ChatRow, Long> {
    fun findByTypeAndIsVisibleToPublicIsTrue(type: Chat.Type, pageable: Pageable): List<ChatRow>
}
