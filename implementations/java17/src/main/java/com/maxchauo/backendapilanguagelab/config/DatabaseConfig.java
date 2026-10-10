package com.maxchauo.backendapilanguagelab.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/** Creates the blocking JDBC connection pool. */
@Configuration
public class DatabaseConfig {
    /** Creates a PostgreSQL Hikari datasource from the shared DATABASE_URL. */
    @Bean
    public HikariDataSource dataSource(
            @Value("${DATABASE_URL}") String databaseUrl) {
        URI databaseUri = parseDatabaseUri(databaseUrl);
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(toJdbcUrl(databaseUri));
        String[] credentials = databaseUri.getUserInfo().split(":", 2);
        dataSource.setUsername(decode(credentials[0]));
        dataSource.setPassword(decode(credentials[1]));
        dataSource.setMaximumPoolSize(10);
        dataSource.setMinimumIdle(1);
        return dataSource;
    }

    private URI parseDatabaseUri(String databaseUrl) {
        URI uri = URI.create(databaseUrl);
        if (!("postgres".equals(uri.getScheme()) || "postgresql".equals(uri.getScheme()))
                || uri.getUserInfo() == null
                || uri.getUserInfo().split(":", 2).length != 2) {
            throw new IllegalArgumentException(
                    "DATABASE_URL must contain a PostgreSQL URL with username and password");
        }
        return uri;
    }

    private String toJdbcUrl(URI uri) {
        int port = uri.getPort() == -1 ? 5432 : uri.getPort();
        String query = uri.getQuery() == null ? "" : "?" + uri.getQuery();
        return "jdbc:postgresql://" + uri.getHost() + ":" + port + uri.getPath() + query;
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
