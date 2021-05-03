package kz.azan.askimam.event.infra

import kz.azan.askimam.meta.DataJdbcIT
import kz.azan.askimam.user.infra.FcmTokenDao
import kz.azan.askimam.user.infra.UserDao
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql

@DataJdbcIT
@Sql("/scripts/users.sql", "/scripts/imams.sql")
internal class GetImamsFcmTokensServiceTest(
    userDao: UserDao,
    fcmTokenDao: FcmTokenDao,
) {

    private val underText = GetImamsFcmTokensService(userDao, fcmTokenDao)

    @Test
    internal fun `should get imams tokens`() {
        val result = underText()

        Assertions.assertThat(result).hasSize(3)
        Assertions.assertThat(result).containsAll(setOf("fcm-1", "fcm-2", "fcm-3"))
    }
}