package kz.azan.askimam.user.domain.model

interface UserRepository {
    fun findById(id: User.Id): User
}