package kz.azan.askimam.chat.web

import kz.azan.askimam.chat.app.usecase.*
import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.chat.domain.model.FcmToken
import kz.azan.askimam.chat.domain.model.Subject
import kz.azan.askimam.chat.web.dto.ChatDto
import kz.azan.askimam.chat.web.dto.CreateChatDto
import kz.azan.askimam.chat.web.dto.UpdateChatDto
import kz.azan.askimam.common.type.NonBlankString
import kz.azan.askimam.common.web.dto.ResponseDto
import kz.azan.askimam.common.web.meta.RestApi
import org.springframework.web.bind.annotation.*

@RestApi("chats")
class ChatController(
    private val getChat: GetChat,
    private val getMyChats: GetMyChats,
    private val getPublicChats: GetPublicChats,
    private val unansweredChats: GetUnansweredChats,
    private val findChats: FindChats,
    private val createChat: CreateChat,
    private val deleteChat: DeleteChat,
    private val updateChatSubject: UpdateChatSubject,
    private val setViewedBy: SetViewedBy,
    private val returnChatToUnansweredList: ReturnChatToUnansweredList,
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

    @GetMapping("unanswered/{offset}/{pageSize}")
    fun unanswered(@PathVariable offset: Int, @PathVariable pageSize: Int): ResponseDto =
        unansweredChats(offset, pageSize).fold(
            { ResponseDto.error(it) },
            { ResponseDto.ok(it.map { projection -> ChatDto.from(projection) }) }
        )

    @GetMapping("find/{phrase}")
    fun find(@PathVariable phrase: String): ResponseDto =
        findChats(phrase).fold(
            { ResponseDto.error(it) },
            { ResponseDto.ok(it.map { projection -> ChatDto.from(projection) }) }
        )

    @GetMapping("messages/{id}")
    fun messages(@PathVariable id: Long): ResponseDto =
        getChat(Chat.Id(id)).fold(
            { ResponseDto.error(it) },
            { ResponseDto.ok(ChatDto.from(it)) }
        )

    @PostMapping
    fun create(@RequestBody dto: CreateChatDto): ResponseDto =
        createChatWithOrWithSubject(dto).fold(
            { ResponseDto.ok() },
            { ResponseDto.error(it) }
        )

    private fun createChatWithOrWithSubject(dto: CreateChatDto) =
        if (dto.subject == null)
            createChat(dto.type, NonBlankString.of(dto.text), FcmToken.from(dto.fcmToken))
        else
            createChat.withSubject(
                dto.type,
                Subject.from(dto.subject),
                NonBlankString.of(dto.text),
                FcmToken.from(dto.fcmToken),
            )

    @DeleteMapping("{id}")
    fun delete(@PathVariable id: Long): ResponseDto =
        deleteChat(Chat.Id(id)).fold(
            { ResponseDto.ok() },
            { ResponseDto.error(it) }
        )

    @PatchMapping("{id}")
    fun update(@PathVariable id: Long, @RequestBody dto: UpdateChatDto): ResponseDto =
        updateChatSubject(Chat.Id(id), Subject.from(dto.subject)).fold(
            { ResponseDto.ok() },
            { ResponseDto.error(it) }
        )

    @PatchMapping("{id}/viewed-by")
    fun setViewedByFlag(@PathVariable id: Long): ResponseDto =
        setViewedBy(Chat.Id(id)).fold(
            { ResponseDto.ok() },
            { ResponseDto.error(it) }
        )

    @PatchMapping("{id}/return-to-unanswered")
    fun returnToUnansweredList(@PathVariable id: Long): ResponseDto =
        returnChatToUnansweredList(Chat.Id(id)).fold(
            { ResponseDto.ok() },
            { ResponseDto.error(it) }
        )
}