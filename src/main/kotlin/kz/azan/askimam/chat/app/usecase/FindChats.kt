package kz.azan.askimam.chat.app.usecase

import io.vavr.control.Either
import kz.azan.askimam.chat.app.port.SearchPort
import kz.azan.askimam.chat.app.projection.ChatProjection
import kz.azan.askimam.chat.domain.repo.ChatRepository
import kz.azan.askimam.common.app.meta.UseCase
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.user.domain.repo.UserRepository

@UseCase
class FindChats(
    private val searchPort: SearchPort,
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
) {

    operator fun invoke(phrase: String): Either<Declination, List<ChatProjection>> = searchPort.find(phrase)
        .flatMap { list -> Either.sequenceRight(list.map { chatRepository.findById(it) }) }
        .map { seq -> seq.map { ChatProjection.from(it, userRepository) } }
        .flatMap { Either.sequenceRight(it) }
        .map { it.toJavaList() }
}
