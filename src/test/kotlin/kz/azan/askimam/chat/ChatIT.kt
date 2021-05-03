package kz.azan.askimam.chat

import kz.azan.askimam.chat.infra.ChatDao
import kz.azan.askimam.chat.infra.ChatRow
import kz.azan.askimam.common.web.dto.ResponseDto
import kz.azan.askimam.meta.IT
import kz.azan.askimam.security.service.JwtService
import kz.azan.askimam.user.infra.UserDao
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.exchange
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate

@IT
@Suppress("UNCHECKED_CAST")
class ChatIT(
    private val rest: TestRestTemplate,
    private val jwtService: JwtService,
    private val jdbcTemplate: JdbcTemplate,
    private val userDao: UserDao,
    private val chatDao: ChatDao,
) : ChatFixtures() {

    private var chatId = 0L

    @BeforeEach
    fun setUp() {
        jdbcTemplate.execute(
            """insert into users(id, username, first_name, last_name, status, password_hash)
                values (1, 'imam', 'Imam', 'Imam', 1, 'x')"""
        )
        jdbcTemplate.execute(
            """insert into users(id, username, first_name, last_name, status, password_hash)
                values (2, 'jon-dow', 'Jon', 'Dow', 1, 'x')"""
        )
        fixtureClock()
        chatId = chatDao.save(ChatRow.from(fixtureSavedChat()).copy(id = null)).id!!
    }

    @AfterEach
    fun tearDown() {
        chatDao.deleteAll()
        userDao.deleteAll()
    }

    @Test
    internal fun `should get public chats`() {
        val entity = rest.getForEntity<ResponseDto>("/chats/public/0/20")

        val list = entity.body?.data as ArrayList<Map<String, Any>>

        assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(entity.body?.status).isEqualTo(ResponseDto.Status.Ok)
        assertThat(list).hasSize(1)
        assertThat(list[0]["id"]).isEqualTo(chatId.toInt())
        assertThat(list[0]["subject"]).isEqualTo("Subject")
    }

    @Test
    internal fun `should get public messages`() {
        val headers = HttpHeaders().apply {
            add(HttpHeaders.AUTHORIZATION, "Bearer ${jwtService.sign(fixtureInquirer).get()}")
        }
        val entity = rest.exchange<ResponseDto>("/chats/messages/$chatId", HttpMethod.GET, HttpEntity<Any>(headers))

        val dto = entity.body?.data as Map<String, Any>
        assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(entity.body?.status).isEqualTo(ResponseDto.Status.Ok)
        assertThat(dto["id"]).isEqualTo(chatId.toInt())
    }
}
