package kz.azan.askimam.meta

import com.fasterxml.jackson.databind.ObjectMapper
import kz.azan.askimam.favorite.FavoriteFixtures
import kz.azan.askimam.user.infra.UserDao
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.TestConstructor.AutowireMode.ALL
import org.springframework.test.web.servlet.MockMvc

@SpringBootTest
@AutoConfigureMockMvc
@TestConstructor(autowireMode = ALL)
class ControllerIT : FavoriteFixtures() {

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var userDao: UserDao

    @Autowired
    protected lateinit var mvc: MockMvc

    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setUp() {
        @Suppress("SpellCheckingInspection")
        jdbcTemplate.execute(
            """insert into users(id, username, first_name, last_name, status, password_hash)
                values (1, 'imam', 'Imam', 'Imam', 1, '${'$'}2y${'$'}12${'$'}9ZZQylVUQDpTROtRg.vmq.6NuH.LBcGC4sTt7G8NXIOic68SeAWYK')"""
        )
        @Suppress("SpellCheckingInspection")
        jdbcTemplate.execute(
            """insert into users(id, username, first_name, last_name, status, password_hash)
                values (2, 'jon-dow', 'Jon', 'Dow', 1, '${'$'}2y${'$'}12${'$'}4C3av3VYh/8CW7ITlH8Yeeza12Q9QR5QdWV04S4HcS896w0l0yBq.')"""
        )
    }

    @AfterEach
    fun tearDown() {
        userDao.deleteAll()
    }
}
