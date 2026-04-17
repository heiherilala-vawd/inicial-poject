package com.example.demo.integration;

import static com.example.demo.integration.conf.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

import com.example.demo.SentryConf;
import com.example.demo.client.api.AuthApi;
import com.example.demo.client.invoker.ApiClient;
import com.example.demo.client.model.AuthResponse;
import com.example.demo.client.model.CrupdateUser;
import com.example.demo.client.model.LoginRequest;
import com.example.demo.endpoint.rest.security.jwt.JwtUtils;
import com.example.demo.integration.conf.AbstractContextInitializer;
import com.example.demo.integration.conf.TestUtils;
import jakarta.transaction.Transactional;
import java.sql.Connection;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = AuthIT.ContextInitializer.class)
@Transactional
@Testcontainers
@Rollback
class AuthIT {
  @Autowired private DataSource dataSource;
  @MockitoBean private SentryConf sentryConf;
  @MockitoBean private JwtUtils jwtServiceMock;
  @MockitoBean private AuthenticationManager authenticationManagerMock;

  private static ApiClient anApiClient() {
    ApiClient client = new ApiClient();
    client.setScheme("http");
    client.setHost("localhost");
    client.setPort(ContextInitializer.SERVER_PORT);
    return client;
  }

  private static ApiClient anApiClientWithToken(String token) {
    return TestUtils.anApiClient(token, ContextInitializer.SERVER_PORT);
  }

  @BeforeEach
  void setUp() throws Exception {
    // Configuration du mock JwtService pour les tokens
    TestUtils.setUpJwtService(jwtServiceMock);
    TestUtils.setUpAuthenticationManager(authenticationManagerMock);
    try (Connection conn = dataSource.getConnection()) {
      ScriptUtils.executeSqlScript(conn, new ClassPathResource("db/testdata/V99_1__testdata.sql"));
    }
  }

  @Test
  void user_can_login_with_valid_credentials() throws Exception {
    ApiClient client = anApiClient();
    AuthApi api = new AuthApi(client);

    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setEmail(ADMIN_EMAIL);
    loginRequest.setPassword(PASSWORD);

    AuthResponse response = api.authLoginPost(loginRequest);

    assertNotNull(response.getToken());
    assertEquals(ADMIN_EMAIL, response.getEmail());
  }

  @Test
  void user_cannot_login_with_invalid_password() {
    ApiClient client = anApiClient();
    AuthApi api = new AuthApi(client);

    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setEmail(ADMIN_EMAIL);
    loginRequest.setPassword("wrongpassword");

    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Les identifications sont erronée\"}",
        () -> api.authLoginPost(loginRequest));
  }

  @Test
  void user_can_get_whoami_with_valid_token() throws Exception {
    // Utiliser un client avec le token ADMIN
    ApiClient client = anApiClientWithToken(ADMIN_TOKEN);
    AuthApi api = new AuthApi(client);

    AuthResponse response = api.authWhoamiGet();

    assertEquals(ADMIN_EMAIL, response.getEmail());
    assertEquals("ADMIN", response.getRole());
  }

  @Test
  void user_cannot_get_whoami_with_invalid_token() {
    ApiClient client = anApiClientWithToken(BAD_TOKEN);
    AuthApi api = new AuthApi(client);
    assertThrowsForbiddenException(() -> api.authWhoamiGet());
  }

  @Test
  void user_can_get_whoami_with_employee_token() throws Exception {
    ApiClient client = anApiClientWithToken(EMPLOYEE_TOKEN);
    AuthApi api = new AuthApi(client);

    AuthResponse response = api.authWhoamiGet();

    assertEquals(EMPLOYEE_EMAIL, response.getEmail());
    assertEquals("EMPLOYEE", response.getRole());
  }

  @Test
  void user_can_get_whoami_with_warehouse_token() throws Exception {
    ApiClient client = anApiClientWithToken(WAREHOUSE_TOKEN);
    AuthApi api = new AuthApi(client);

    AuthResponse response = api.authWhoamiGet();

    assertEquals(WAREHOUSE_EMAIL, response.getEmail());
    assertEquals("WAREHOUSE_WORKER", response.getRole());
  }

  @Test
  @Transactional
  @Rollback
  void user_can_register_new_account() throws Exception {
    ApiClient client = anApiClient();
    AuthApi api = new AuthApi(client);

    CrupdateUser newUser = someCreatableUser();

    AuthResponse response = api.authRegisterPost(newUser);

    assertNotNull(response.getToken());
    assertEquals(newUser.getEmail(), response.getEmail());
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
