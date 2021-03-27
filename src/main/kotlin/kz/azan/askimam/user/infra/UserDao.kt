package kz.azan.askimam.user.infra

import org.springframework.data.repository.CrudRepository

interface UserDao : CrudRepository<UserRow, Int>
