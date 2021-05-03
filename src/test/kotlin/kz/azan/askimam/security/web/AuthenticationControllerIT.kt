package kz.azan.askimam.security.web

import kz.azan.askimam.meta.ControllerIT
import kz.azan.askimam.security.web.dto.LoginDto
import kz.azan.askimam.user.domain.model.User.Type.Inquirer
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.post

internal class AuthenticationControllerIT : ControllerIT() {

    private val url = "/auth"

    @Test
    internal fun `should authenticate a user`() {
        mvc.post("$url/login") {
            contentType = APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                LoginDto(
                    "jon-dow",
                    "passwd",
                    fixtureInquirerFcmToken.value.value,
                )
            )
        }.andExpect {
            status { isOk() }
            jsonPath("\$.status") { value("Ok") }
            jsonPath("\$.data.jwt") { value(Matchers.startsWith("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJhc2t")) }
            jsonPath("\$.data.userId") { value(fixtureInquirerId.value) }
            jsonPath("\$.data.userType") { value(Inquirer.name) }
        }
    }
}
