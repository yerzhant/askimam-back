package kz.azan.askimam.chat.app.usecase

import io.mockk.every
import io.mockk.mockk
import io.vavr.kotlin.left
import io.vavr.kotlin.right
import kz.azan.askimam.chat.ChatFixtures
import kz.azan.askimam.chat.app.port.SearchPort
import kz.azan.askimam.chat.domain.model.Subject
import kz.azan.askimam.common.domain.Declination
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class FindChatsTest : ChatFixtures() {

    private val searchPort = mockk<SearchPort>()

    @Test
    internal fun `should get the list`() {
        fixtureClock()
        every { searchPort.find("some phrase") } returns right(listOf(fixtureChatId1, fixtureChatId2))
        every { chatRepository.findById(fixtureChatId1) } returns right(fixtureSavedTwoChats().first())
        every { chatRepository.findById(fixtureChatId2) } returns right(fixtureSavedTwoChats().last())

        val list = FindChats(searchPort, chatRepository, userRepository)("some phrase").get()

        assertThat(list).hasSize(2)

        assertThat(list.first().id).isEqualTo(fixtureChatId1)
        assertThat(list.first().subject).isEqualTo(fixtureSubject)

        assertThat(list.last().id).isEqualTo(fixtureChatId2)
        assertThat(list.last().subject).isEqualTo(Subject(fixtureMessage))
    }

    @Test
    internal fun `should get an empty list`() {
        every { searchPort.find("some phrase") } returns right(emptyList())

        val list = FindChats(searchPort, chatRepository, userRepository)("some phrase").get()

        assertThat(list).isEmpty()
    }

    @Test
    internal fun `should not get the list`() {
        fixtureClock()
        every { searchPort.find("some phrase") } returns right(listOf(fixtureChatId1, fixtureChatId2))
        every { chatRepository.findById(fixtureChatId1) } returns right(fixtureSavedTwoChats().first())
        every { chatRepository.findById(fixtureChatId2) } returns left(Declination.withReason("x"))

        val result = FindChats(searchPort, chatRepository, userRepository)("some phrase")

        assertThat(result.left.reason.value).isEqualTo("x")
    }

    @Test
    internal fun `should not get the list either`() {
        fixtureClock()
        every { searchPort.find("some phrase") } returns right(listOf(fixtureChatId1, fixtureChatId2))
        every { chatRepository.findById(fixtureChatId1) } returns left(Declination.withReason("x"))
        every { chatRepository.findById(fixtureChatId2) } returns right(fixtureSavedTwoChats().last())

        val result = FindChats(searchPort, chatRepository, userRepository)("some phrase")

        assertThat(result.left.reason.value).isEqualTo("x")
    }

    @Test
    internal fun `should not get the list too`() {
        every { searchPort.find("some phrase") } returns left(Declination.withReason("x"))

        val result = FindChats(searchPort, chatRepository, userRepository)("some phrase")

        assertThat(result.left.reason.value).isEqualTo("x")
    }
}
