package kz.azan.askimam.chat.web

import kz.azan.askimam.chat.app.usecase.AddAudioMessage
import kz.azan.askimam.chat.app.usecase.AddTextMessage
import kz.azan.askimam.chat.app.usecase.DeleteMessage
import kz.azan.askimam.chat.app.usecase.UpdateTextMessage
import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.chat.domain.model.FcmToken
import kz.azan.askimam.chat.domain.model.Message
import kz.azan.askimam.chat.web.dto.AddAudioMessageDto
import kz.azan.askimam.chat.web.dto.AddTextMessageDto
import kz.azan.askimam.chat.web.dto.UpdateTextMessageDto
import kz.azan.askimam.common.type.NonBlankString
import kz.azan.askimam.common.web.dto.ResponseDto
import kz.azan.askimam.common.web.meta.RestApi
import org.springframework.web.bind.annotation.*

@RestApi("messages")
class MessageController(
    private val addTextMessage: AddTextMessage,
    private val addAudioMessage: AddAudioMessage,
    private val deleteMessage: DeleteMessage,
    private val updateTextMessage: UpdateTextMessage,
) {

    @PostMapping
    fun addText(@RequestBody dto: AddTextMessageDto): ResponseDto =
        addTextMessage(Chat.Id(dto.chatId), NonBlankString.of(dto.text), FcmToken.from(dto.fcmToken)).fold(
            { ResponseDto.ok() },
            { ResponseDto.error(it) }
        )

    @PostMapping("audio")
    fun addAudio(@RequestBody dto: AddAudioMessageDto): ResponseDto =
        addAudioMessage(Chat.Id(dto.chatId), NonBlankString.of(dto.audio), FcmToken.from(dto.fcmToken)).fold(
            { ResponseDto.ok() },
            { ResponseDto.error(it) }
        )

    @DeleteMapping("{chatId}/{messageId}")
    fun delete(@PathVariable chatId: Long, @PathVariable messageId: Long): ResponseDto =
        deleteMessage(Chat.Id(chatId), Message.Id(messageId)).fold(
            { ResponseDto.ok() },
            { ResponseDto.error(it) }
        )

    @PatchMapping("{chatId}/{messageId}")
    fun updateText(
        @PathVariable chatId: Long,
        @PathVariable messageId: Long,
        @RequestBody dto: UpdateTextMessageDto,
    ): ResponseDto =
        updateTextMessage(
            Chat.Id(chatId),
            Message.Id(messageId),
            NonBlankString.of(dto.text),
            FcmToken.from(dto.fcmToken),
        ).fold(
            { ResponseDto.ok() },
            { ResponseDto.error(it) }
        )
}