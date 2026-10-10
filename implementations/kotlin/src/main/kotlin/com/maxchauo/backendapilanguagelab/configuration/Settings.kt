package com.maxchauo.backendapilanguagelab.configuration

/** Runtime configuration loaded from environment variables. */
data class Settings(
    val databaseUrl: String,
    val serverPort: Int,
) {
    companion object {
        /** Loads and validates application settings from the process environment. */
        fun fromEnvironment(environment: Map<String, String> = System.getenv()): Settings {
            val databaseUrl = environment["DATABASE_URL"]
                ?: error("DATABASE_URL is required")
            val serverPort = environment["SERVER_PORT"]?.toIntOrNull() ?: 8080
            require(serverPort in 1..65_535) {
                "SERVER_PORT must be an integer between 1 and 65535"
            }
            return Settings(databaseUrl, serverPort)
        }
    }
}
