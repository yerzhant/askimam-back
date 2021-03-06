package kz.azan.askimam.user.infra.dao

import kz.azan.askimam.user.infra.model.UserRow
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
interface UserDao : CrudRepository<UserRow, Int> {
    fun findByUsernameAndStatus(username: String?, status: Int): UserRow?

    @Query("select u.* from users u join auth_assignment a on u.id = a.user_id and item_name = :role")
    fun findAllByRole(role: String): List<UserRow>
}