package kz.azan.askimam.chat

import kz.azan.askimam.common.web.dto.ResponseDto
import kz.azan.askimam.meta.IT
import kz.azan.askimam.security.service.JwtService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.exchange
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus

@IT
@Suppress("UNCHECKED_CAST")
class ChatIT(
    private val rest: TestRestTemplate,
    private val jwtService: JwtService,
) : ChatFixtures() {

//    @Test
    internal fun `should get public chats`() {
        val entity = rest.getForEntity<ResponseDto>("/chats/public/0/20")

        val list = entity.body?.data as ArrayList<Map<String, Any>>

        assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(entity.body?.status).isEqualTo(ResponseDto.Status.Ok)
        assertThat(list).hasSize(2)
        assertThat(list[0]["id"]).isEqualTo(1)
        assertThat(list[0]["subject"]).isEqualTo("Subject")
    }

//    @Test
//    @Disabled
    internal fun `should get public messages`() {
        val headers = HttpHeaders().apply {
            add(HttpHeaders.AUTHORIZATION, "Bearer ${jwtService.sign(fixtureInquirer).get()}")
        }
        val entity = rest.exchange<ResponseDto>("/chats/messages/1", HttpMethod.GET, HttpEntity<Any>(headers))

        val dto = entity.body?.data as Map<String, Any>
        assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(entity.body?.status).isEqualTo(ResponseDto.Status.Ok)
        assertThat(dto["id"]).isEqualTo(1)
    }
}
