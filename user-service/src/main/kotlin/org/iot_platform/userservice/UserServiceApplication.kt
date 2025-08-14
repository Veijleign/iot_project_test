package org.iot_platform.userservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@SpringBootApplication
@EnableR2dbcRepositories(basePackages = ["org.iot_platform.userservice.domain.repository"])
class UserServiceApplication

fun main(args: Array<String>) {
    runApplication<UserServiceApplication>(*args)
}
