package kz.azan.askimam.chat.web

import kz.azan.askimam.chat.app.usecase.GetChat
import kz.azan.askimam.chat.app.usecase.GetMyChats
import kz.azan.askimam.chat.app.usecase.GetPublicChats
import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.chat.web.dto.ChatDto
import kz.azan.askimam.common.web.dto.ResponseDto
import kz.azan.askimam.common.web.meta.RestApi
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@RestApi("chats")
class ChatController(
    private val getPublicChats: GetPublicChats,
    private val getMyChats: GetMyChats,
    private val getChat: GetChat,
) {

    @GetMapping("public/{offset}/{pageSize}")
    fun public(@PathVariable offset: Int, @PathVariable pageSize: Int): ResponseDto =
        getPublicChats(offset, pageSize).fold(
            { ResponseDto.error(it) },
            { ResponseDto.ok(it.map { projection -> ChatDto.from(projection) }) }
        )

    @GetMapping("my/{offset}/{pageSize}")
    fun my(@PathVariable offset: Int, @PathVariable pageSize: Int): ResponseDto =
        getMyChats(offset, pageSize).fold(
            { ResponseDto.error(it) },
            { ResponseDto.ok(it.map { projection -> ChatDto.from(projection) }) }
        )

    @GetMapping("messages/{id}")
    fun messages(@PathVariable id: Long): ResponseDto =
        getChat(Chat.Id(id)).fold(
            { ResponseDto.error(it) },
            { ResponseDto.ok(ChatDto.from(it)) }
        )
}