package kz.azan.askimam.chat.infra.adapter

import io.mockk.every
import io.mockk.mockk
import kz.azan.askimam.chat.domain.model.Chat
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject

internal class SearchAdapterTest {

    private val restTemplate = mockk<RestTemplate>()

    private val underTest = SearchAdapter(restTemplate)

    @Test
    internal fun `should return chat ids`() {
        every { restTemplate.getForObject<List<Long>>("/search/Hi") } returns listOf(1, 2)

        val result = underTest.find("Hi")

        assertThat(result.get()).isEqualTo(listOf(Chat.Id(1), Chat.Id(2)))
    }

    @Test
    internal fun `should not return chat ids`() {
        every { restTemplate.getForObject<List<Long>>("/search/Hi") } throws RestClientException("x")

        val result = underTest.find("Hi")

        assertThat(result.left.reason.value).isEqualTo("x")
    }
}
