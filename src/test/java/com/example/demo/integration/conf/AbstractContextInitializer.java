package com.example.demo.integration.conf;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.PostgreSQLContainer;

public abstract class AbstractContextInitializer
    implements ApplicationContextInitializer<ConfigurableApplicationContext> {

  @Override
  public void initialize(ConfigurableApplicationContext applicationContext) {
    String flywayTestdataPath = "classpath:/db/testdata";

    String jdbcUrl;
    String username;
    String password;

    // Utiliser Testcontainers en local
    PostgreSQLContainer<?> postgresContainer =
        new PostgreSQLContainer<>("postgres:15.2")
            .withDatabaseName("test-db")
            .withUsername("test")
            .withPassword("test");
    postgresContainer.start();
    jdbcUrl = postgresContainer.getJdbcUrl();
    username = postgresContainer.getUsername();
    password = postgresContainer.getPassword();
    System.out.println("=== Running locally, using Testcontainers ===");

    TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
        applicationContext,
        "server.port=" + this.getServerPort(),
        "spring.datasource.url=" + jdbcUrl,
        "spring.datasource.username=" + username,
        "spring.datasource.password=" + password,
        "spring.flyway.locations=classpath:/db/migration," + flywayTestdataPath,
        "jwt.secret.key=test-secret-key-test-secret-key-test",
        "jwt.expiration.time=86400000");
  }

  public abstract int getServerPort();
}
