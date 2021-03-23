package kz.azan.askimam.common.infra

import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.TestConstructor.AutowireMode.ALL
import kotlin.annotation.AnnotationTarget.CLASS

@Target(CLASS)
@DataJdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@TestConstructor(autowireMode = ALL)
annotation class DataJdbcIT
