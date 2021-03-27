package kz.azan.askimam.user.infra

import org.springframework.data.repository.CrudRepository
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
interface UserDao : CrudRepository<UserRow, Int> {
    fun findByUsernameAndStatus(username: String?, status: Int): UserRow?
}
