package com.maxchauo.backendapilanguagelab

import com.maxchauo.backendapilanguagelab.configuration.Settings
import com.maxchauo.backendapilanguagelab.jobs.JobsService
import com.maxchauo.backendapilanguagelab.jobs.PostgresJobsRepository
import com.maxchauo.backendapilanguagelab.jobs.JobsRepository
import com.maxchauo.backendapilanguagelab.jobs.configureRouting
import com.maxchauo.backendapilanguagelab.jobs.configureStatusPages
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/** Starts the Ktor server. */
fun main() {
    embeddedServer(
        factory = Netty,
        port = Settings.fromEnvironment().serverPort,
        host = "0.0.0.0",
        module = { module() },
    ).start(wait = true)
}

/** Configures the Ktor application modules. */
fun Application.module(repository: JobsRepository? = null) {
    val settings = Settings.fromEnvironment()
    val dataSource = if (repository == null) createDataSource(settings) else null
    val jobsRepository = repository ?: PostgresJobsRepository(requireNotNull(dataSource))
    val jobsService = JobsService(jobsRepository)

    install(ContentNegotiation) {
        json(Json { encodeDefaults = true })
    }
    configureStatusPages()
    configureRouting(jobsService)

    if (dataSource != null) {
        monitor.subscribe(ApplicationStopped) {
            dataSource.close()
        }
    }
}

private fun createDataSource(settings: Settings): HikariDataSource {
    val uri = URI(settings.databaseUrl)
    val credentials = uri.userInfo?.split(":", limit = 2)
        ?: error("DATABASE_URL must include username and password")
    require(credentials.size == 2) { "DATABASE_URL must include username and password" }

    val configuration = HikariConfig().apply {
        jdbcUrl = "jdbc:postgresql://${uri.host}:${uri.port}${uri.path}?${uri.query.orEmpty()}"
        username = URLDecoder.decode(credentials[0], StandardCharsets.UTF_8)
        password = URLDecoder.decode(credentials[1], StandardCharsets.UTF_8)
        maximumPoolSize = 10
        minimumIdle = 1
        poolName = "jobs-pool"
    }
    return HikariDataSource(configuration)
}
