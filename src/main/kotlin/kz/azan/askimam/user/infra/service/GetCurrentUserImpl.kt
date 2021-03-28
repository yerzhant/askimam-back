package kz.azan.askimam.user.infra.service

import kz.azan.askimam.user.domain.model.User
import kz.azan.askimam.user.domain.service.GetCurrentUser
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class GetCurrentUserImpl : GetCurrentUser {
    override fun invoke() = SecurityContextHolder.getContext().authentication.principal as User
}