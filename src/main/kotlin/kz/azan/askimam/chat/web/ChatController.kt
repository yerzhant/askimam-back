package kz.azan.askimam.chat.web

import kz.azan.askimam.chat.app.usecase.GetPublicChats
import kz.azan.askimam.chat.web.dto.ChatProjectionDto
import kz.azan.askimam.common.web.dto.ResponseDto
import kz.azan.askimam.common.web.meta.RestApi
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@RestApi("chats")
class ChatController(
    private val getPublicChats: GetPublicChats,
) {

    @GetMapping("public/{offset}/{pageSize}")
    fun public(@PathVariable offset: Int, @PathVariable pageSize: Int): ResponseDto =
        getPublicChats(offset, pageSize).fold(
            { ResponseDto.error(it) },
            { ResponseDto.ok(it.map { projection -> ChatProjectionDto.from(projection) }) }
        )
}