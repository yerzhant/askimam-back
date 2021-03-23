package kz.azan.askimam.chat.infra

import io.mockk.every
import io.mockk.mockk
import kz.azan.askimam.chat.domain.model.ChatFixtures
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*

internal class ChatJdbcRepositoryTest : ChatFixtures() {

    private val dao = mockk<ChatDao>()

    @Test
    internal fun `should find by id`() {
        fixtureClock()
        every { dao.findById(1) } returns Optional.of(ChatRow.from(fixtureSavedChat()))

        val chat = ChatJdbcRepository(clock, eventPublisher, getCurrentUser, dao).findById(fixtureChatId).get()

        assertThat(chat.id).isEqualTo(fixtureChatId)
    }
}