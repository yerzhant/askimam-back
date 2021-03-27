package kz.azan.askimam.chat.app.usecase

import io.vavr.control.Option
import io.vavr.kotlin.some
import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.chat.domain.model.ChatRepository
import kz.azan.askimam.user.domain.service.GetCurrentUser
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.user.domain.model.User.Type.Imam
import kz.azan.askimam.user.domain.model.User.Type.Inquirer

class SetViewedBy(
    private val getCurrentUser: GetCurrentUser,
    private val chatRepository: ChatRepository,
) {
    operator fun invoke(id: Chat.Id): Option<Declination> = chatRepository.findById(id).fold(
        { some(it) },
        {
            when (getCurrentUser().type) {
                Imam -> it.viewedByImam()
                Inquirer -> it.viewedByInquirer()
            }.orElse {
                chatRepository.update(it)
            }
        }
    )
}