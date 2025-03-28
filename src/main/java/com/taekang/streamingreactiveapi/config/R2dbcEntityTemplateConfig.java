package com.taekang.streamingreactiveapi.config;

import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.transaction.ReactiveTransactionManager;

@Configuration
@EnableR2dbcRepositories(
        basePackages = "com.taekang.streamingreactiveapi.repository.leagueInfo"
)
public class R2dbcEntityTemplateConfig {

    @Value("${spring.r2dbc.streaming.host}")
    private String host;

    @Value("${spring.r2dbc.streaming.database}")
    private String database;

    @Value("${spring.r2dbc.streaming.username}")
    private String username;

    @Value("${spring.r2dbc.streaming.password}")
    private String password;

    @Bean
    @ConfigurationProperties(prefix = "spring.r2dbc.streaming")
    public ConnectionFactory leagueInfoConnectionFactory() {
        return ConnectionFactories.get(ConnectionFactoryOptions.builder()
                .option(ConnectionFactoryOptions.DRIVER, "mariadb")
                .option(ConnectionFactoryOptions.HOST, host)
                .option(ConnectionFactoryOptions.PORT, 3306)
                .option(ConnectionFactoryOptions.DATABASE, database)
                .option(ConnectionFactoryOptions.USER, username)
                .option(ConnectionFactoryOptions.PASSWORD, password)
                .build());
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.r2dbc.user")
    public ConnectionFactory userConnectionFactory() {
        return ConnectionFactories.get(ConnectionFactoryOptions.builder()
                .option(ConnectionFactoryOptions.DRIVER, "mariadb")
                .option(ConnectionFactoryOptions.HOST, "localhost")
                .option(ConnectionFactoryOptions.PORT, 3306)
                .option(ConnectionFactoryOptions.DATABASE, "user")
                .option(ConnectionFactoryOptions.USER, "root")
                .option(ConnectionFactoryOptions.PASSWORD, "250225")
                .build());
    }

    @Bean
    public DatabaseClient leagueInfoDatabaseClient(@Qualifier("leagueInfoConnectionFactory") ConnectionFactory connectionFactory) {
        return DatabaseClient.create(connectionFactory);
    }

    @Bean
    public ReactiveTransactionManager leagueInfoTransactionManager(@Qualifier("leagueInfoConnectionFactory") ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }

    @Bean
    public ReactiveTransactionManager userTransactionManager(@Qualifier("userConnectionFactory") ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }
}
