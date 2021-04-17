package kz.azan.askimam.meta

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.TestConstructor.AutowireMode.ALL
import org.springframework.test.context.jdbc.Sql

@TestConstructor(autowireMode = ALL)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Sql("/scripts/users.sql", "/scripts/chats.sql", "/scripts/favorites.sql")
annotation class IT
