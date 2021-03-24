package kz.azan.askimam.chat.infra

import org.springframework.data.repository.CrudRepository

interface ChatDao : CrudRepository<ChatRow, Long>
