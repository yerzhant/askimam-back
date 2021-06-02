package kz.azan.askimam.chat.app.usecase

import io.vavr.control.Either
import kz.azan.askimam.chat.app.projection.ChatProjection
import kz.azan.askimam.common.app.meta.UseCase
import kz.azan.askimam.common.domain.Declination

@UseCase
class FindChats {

    operator fun invoke(phrase: String): Either<Declination, List<ChatProjection>> = TODO()
}
