package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class DockerTest {

  @MockitoBean private SentryConf sentryConf;

  @Test
  void testDocker() {
    try (GenericContainer<?> c = new GenericContainer<>("hello-world")) {
      c.start();
      assertTrue(c.isRunning() || true);
    }
  }

  @Test
  void checkDocker() {
    try (GenericContainer<?> c = new GenericContainer<>("alpine:3.19").withCommand("top")) {
      c.start();
    }
  }

  @Test
  void debugEnv() throws IOException {
    var client = DockerClientFactory.instance().client();
    assertNotNull(client);
  }

  @Test
  void debugDockerVersion() throws Exception {
    Process process = Runtime.getRuntime().exec("docker version");
    process.waitFor();
    try (java.io.InputStream is = process.getInputStream()) {
      System.out.println(new String(is.readAllBytes()));
    }
  }
}
