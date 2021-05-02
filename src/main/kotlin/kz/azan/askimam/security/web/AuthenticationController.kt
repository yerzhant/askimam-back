package kz.azan.askimam.security.web

import kz.azan.askimam.common.web.meta.RestApi
import kz.azan.askimam.security.web.dto.LoginDto
import kz.azan.askimam.security.web.usecase.Login
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@RestApi("auth")
class AuthenticationController(
    private val login: Login,
) {

    @PostMapping("login")
    fun doLogin(@Validated @RequestBody dto: LoginDto) = login(dto)
}
