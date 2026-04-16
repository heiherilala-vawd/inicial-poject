package com.example.demo.integration.conf;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public abstract class AbstractContextInitializer
    implements ApplicationContextInitializer<ConfigurableApplicationContext> {

  // Utiliser une version plus récente de PostgreSQL
  private static final PostgreSQLContainer<?> postgresContainer;

  static {
    postgresContainer =
        new PostgreSQLContainer<>(DockerImageName.parse("postgres:16.2"))
            .withDatabaseName("testdb")
            .withUsername("postgres")
            .withPassword("00001111");
    postgresContainer.start();
  }

  @Override
  public void initialize(ConfigurableApplicationContext applicationContext) {
    String flywayTestdataPath = "classpath:/db/testdata";
    TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
        applicationContext,
        "server.port=" + this.getServerPort(),
        "spring.datasource.url=" + postgresContainer.getJdbcUrl(),
        "spring.datasource.username=" + postgresContainer.getUsername(),
        "spring.datasource.password=" + postgresContainer.getPassword(),
        "spring.flyway.locations=classpath:/db/migration," + flywayTestdataPath,
        "jwt.secret.key=test-secret-key-test-secret-key-test",
        "jwt.expiration.time=86400000");
  }

  public abstract int getServerPort();
}
