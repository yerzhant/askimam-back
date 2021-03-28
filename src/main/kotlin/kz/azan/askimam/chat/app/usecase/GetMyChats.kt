package kz.azan.askimam.chat.app.usecase

import io.vavr.control.Either
import kz.azan.askimam.chat.app.projection.ChatProjection
import kz.azan.askimam.chat.domain.model.ChatRepository
import kz.azan.askimam.common.app.meta.UseCase
import kz.azan.askimam.common.domain.Declination

@UseCase
class GetMyChats(
    private val chatRepository: ChatRepository,
) {

    operator fun invoke(offset: Int, pageSize: Int): Either<Declination, List<ChatProjection>> =
        chatRepository.findMyChats(offset, pageSize).map { chats ->
            chats.map { chat ->
                ChatProjection(chat.id!!, chat.subjectText())
            }
        }
}