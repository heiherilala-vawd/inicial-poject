package com.example.demo.integration.conf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.demo.client.invoker.ApiClient;
import com.example.demo.client.invoker.ApiException;
import com.example.demo.client.model.CrupdateUser;
import com.example.demo.client.model.Role;
import com.example.demo.client.model.Sex;
import com.example.demo.client.model.User;
import com.example.demo.endpoint.rest.security.jwt.JwtUtils;
import java.io.IOException;
import java.net.ServerSocket;
import java.time.Instant;
import java.util.UUID;
import lombok.SneakyThrows;
import org.junit.jupiter.api.function.Executable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class TestUtils {

  // IDs de test
  public static final String ADMIN_ID = "admin1_id";
  public static final String WAREHOUSE_ID = "warehouse1_id";
  public static final String EMPLOYEE_ID = "employee1_id";
  public static final String ADMINISTRATION_ID = "admin2_id";
  public static final String USER1_ID = "user1_id";
  public static final String USER2_ID = "user2_id";
  public static final String USER3_ID = "user3_id";

  // Emails de test
  public static final String ADMIN_EMAIL = "admin@hei.school";
  public static final String WAREHOUSE_EMAIL = "warehouse@hei.school";
  public static final String EMPLOYEE_EMAIL = "employee@hei.school";
  public static final String ADMINISTRATION_EMAIL = "admin2@hei.school";
  public static final String USER1_EMAIL = "alice@hei.school";
  public static final String USER2_EMAIL = "bob@hei.school";
  public static final String RANDOM_EMAIL = "RANDOM_EMAIL@hei.school";

  public static final String PASSWORD = "password";

  public static final Authentication ADMIN_AUTH = mock(Authentication.class);
  public static final Authentication WAREHOUSE_AUTH = mock(Authentication.class);
  public static final Authentication EMPLOYEE_AUTH = mock(Authentication.class);
  public static final Authentication ADMINISTRATION_AUTH = mock(Authentication.class);
  public static final Authentication USER1_AUTH = mock(Authentication.class);
  public static final Authentication USER2_AUTH = mock(Authentication.class);
  public static final Authentication RANDOM_AUTH = mock(Authentication.class);

  // Tokens de test (ce sont juste des identifiants, pas de vrais JWT)
  public static final String BAD_TOKEN = "bad_token";
  public static final String ADMIN_TOKEN = "admin_token";
  public static final String WAREHOUSE_TOKEN = "warehouse_token";
  public static final String EMPLOYEE_TOKEN = "employee_token";
  public static final String ADMINISTRATION_TOKEN = "administration_token";
  public static final String USER1_TOKEN = "user1_token";
  public static final String USER2_TOKEN = "user2_token";
  public static final String RANDOM_TOKEN = "random2_token";

  public static final String INTERNAL_SERVER_ERROR = "{\"type\":\"500 INTERNAL_SERVER_ERROR\",\"message\":\"Access Denied\"}";

  private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  public static void setUpJwtService(JwtUtils jwtServiceMock) {

    // =========================
    // TOKEN -> EMAIL
    // =========================
    when(jwtServiceMock.getUserEmailFromJwtToken(anyString()))
        .thenAnswer(
            invocation -> {
              String token = invocation.getArgument(0);

              if (ADMIN_TOKEN.equals(token)) return ADMIN_EMAIL;
              if (WAREHOUSE_TOKEN.equals(token)) return WAREHOUSE_EMAIL;
              if (EMPLOYEE_TOKEN.equals(token)) return EMPLOYEE_EMAIL;
              if (ADMINISTRATION_TOKEN.equals(token)) return ADMINISTRATION_EMAIL;
              if (USER1_TOKEN.equals(token)) return USER1_EMAIL;
              if (USER2_TOKEN.equals(token)) return USER2_EMAIL;
              if (RANDOM_TOKEN.equals(token)) return RANDOM_EMAIL;

              if (BAD_TOKEN.equals(token)) return null;

              return null; // token inconnu
            });

    // =========================
    // AUTH -> TOKEN
    // =========================
    when(jwtServiceMock.generateJwtToken(any()))
        .thenAnswer(
            invocation -> {
              Object auth = invocation.getArgument(0);

              if (ADMIN_AUTH.equals(auth)) return ADMIN_TOKEN;
              if (WAREHOUSE_AUTH.equals(auth)) return WAREHOUSE_TOKEN;
              if (EMPLOYEE_AUTH.equals(auth)) return EMPLOYEE_TOKEN;
              if (ADMINISTRATION_AUTH.equals(auth)) return ADMINISTRATION_TOKEN;
              if (USER1_AUTH.equals(auth)) return USER1_TOKEN;
              if (USER2_AUTH.equals(auth)) return USER2_TOKEN;
              if (RANDOM_AUTH.equals(auth)) return RANDOM_TOKEN;

              return null;
            });

    // =========================
    // VALIDATION TOKEN
    // =========================
    when(jwtServiceMock.validateJwtToken(anyString()))
        .thenAnswer(
            invocation -> {
              String token = invocation.getArgument(0);

              return ADMIN_TOKEN.equals(token)
                  || WAREHOUSE_TOKEN.equals(token)
                  || EMPLOYEE_TOKEN.equals(token)
                  || ADMINISTRATION_TOKEN.equals(token)
                  || USER1_TOKEN.equals(token)
                  || USER2_TOKEN.equals(token)
                  || RANDOM_TOKEN.equals(token);
            });
  }

  public static void setUpAuthenticationManager(AuthenticationManager authenticationManagerMock) {

    when(authenticationManagerMock.authenticate(any()))
        .thenAnswer(
            invocation -> {
              UsernamePasswordAuthenticationToken auth = invocation.getArgument(0);

              String email = auth.getName();
              String password = (String) auth.getCredentials();

              // =========================
              // CHECK PASSWORD
              // =========================
              if (!PASSWORD.equals(password)) {
                throw new BadCredentialsException("Les identifications sont erronée");
              }

              // =========================
              // EMAIL -> AUTH
              // =========================
              if (ADMIN_EMAIL.equals(email)) return ADMIN_AUTH;
              if (EMPLOYEE_EMAIL.equals(email)) return EMPLOYEE_AUTH;
              if (ADMINISTRATION_EMAIL.equals(email)) return ADMINISTRATION_AUTH;
              if (USER1_EMAIL.equals(email)) return USER1_AUTH;
              if (USER2_EMAIL.equals(email)) return USER2_AUTH;
              if (WAREHOUSE_EMAIL.equals(email)) return WAREHOUSE_AUTH;
              if (RANDOM_EMAIL.equals(email)) return RANDOM_AUTH;

              throw new BadCredentialsException("Les identifications sont erronée");
            });
  }

  public static ApiClient anApiClient(String token, int serverPort) {
    ApiClient client = new ApiClient();
    client.setScheme("http");
    client.setHost("localhost");
    client.setPort(serverPort);
    client.setRequestInterceptor(
        httpRequestBuilder -> httpRequestBuilder.header("Authorization", "Bearer " + token));
    return client;
  }

  public static User admin1() {
    User user = new User();
    user.setId(ADMIN_ID);
    user.setRole(Role.ADMIN);
    user.setFirstName("Admin");
    user.setLastName("System");
    user.setSex(Sex.M);
    user.setEmail(ADMIN_EMAIL);
    user.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));
    user.setUpdatedAt(Instant.parse("2024-01-01T00:00:00Z"));
    return user;
  }

  public static User warehouseWorker1() {
    User user = new User();
    user.setId(WAREHOUSE_ID);
    user.setRole(Role.WAREHOUSE_WORKER);
    user.setFirstName("Warehouse");
    user.setLastName("Worker");
    user.setSex(Sex.M);
    user.setEmail(WAREHOUSE_EMAIL);
      user.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));
      user.setUpdatedAt(Instant.parse("2024-01-01T00:00:00Z"));
    return user;
  }

  public static User employee1() {
    User user = new User();
    user.setId(EMPLOYEE_ID);
    user.setRole(Role.EMPLOYEE);
    user.setFirstName("John");
    user.setLastName("Doe");
    user.setSex(Sex.M);
    user.setEmail(EMPLOYEE_EMAIL);
      user.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));
      user.setUpdatedAt(Instant.parse("2024-01-01T00:00:00Z"));
    return user;
  }

  public static User administration1() {
    User user = new User();
    user.setId(ADMINISTRATION_ID);
    user.setRole(Role.ADMINISTRATION);
    user.setFirstName("Admin");
    user.setLastName("Staff");
    user.setSex(Sex.F);
    user.setEmail(ADMINISTRATION_EMAIL);
      user.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));
      user.setUpdatedAt(Instant.parse("2024-01-01T00:00:00Z"));
    return user;
  }

  public static User user1() {
    User user = new User();
    user.setId(USER1_ID);
    user.setRole(Role.EMPLOYEE);
    user.setFirstName("Alice");
    user.setLastName("Martin");
    user.setSex(Sex.F);
    user.setEmail("alice@hei.school");
      user.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));
      user.setUpdatedAt(Instant.parse("2024-01-01T00:00:00Z"));
    return user;
  }

  public static User user2() {
    User user = new User();
    user.setId(USER2_ID);
    user.setRole(Role.EMPLOYEE);
    user.setFirstName("Bob");
    user.setLastName("Bernard");
    user.setSex(Sex.M);
    user.setEmail("bob@hei.school");
      user.setCreatedAt(Instant.parse(""));
      user.setUpdatedAt(Instant.parse(""));
    return user;
  }

  public static CrupdateUser someCreatableUser() {
    double random = Math.random();
    int number = (int) (random * 1000);
    String stringRandom = String.format("%03d", number);
    CrupdateUser user = new CrupdateUser();
    user.setFirstName("Test");
    user.setLastName("User");
    user.setEmail(RANDOM_EMAIL);
    user.setPassword(PASSWORD);
    user.setSex(Sex.M);
    user.setRole(Role.EMPLOYEE);
    return user;
  }


  public static CrupdateUser userToCrupdateUser(User user){
    CrupdateUser crupdateUser = new CrupdateUser();
    crupdateUser.setFirstName(user.getFirstName());
    crupdateUser.setLastName(user.getLastName());
    crupdateUser.setEmail(user.getEmail());
    crupdateUser.setPassword(PASSWORD);
    crupdateUser.setSex(user.getSex());
    crupdateUser.setRole(user.getRole());
    return crupdateUser;
  };

  public static int anAvailableRandomPort() {
    try {
      return new ServerSocket(0).getLocalPort();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @SneakyThrows
  public static void assertThrowsApiException(String expectedMessage, Executable executable) {
    ApiException exception = assertThrows(ApiException.class, executable);
    assertEquals(expectedMessage, exception.getResponseBody());
  }

  public static void assertThrowsForbiddenException(Executable executable) {
    ApiException exception = assertThrows(ApiException.class, executable);
    assertEquals(
        "{\"type\":\"Full authentication is required to access this resource\",\"message\":\"NotAuthorizedException\"}",
        exception.getResponseBody());
  }

  @FunctionalInterface
  public interface ThrowingRunnable {
    void run() throws Exception;
  }

  public static boolean isValidUUID(String candidate) {
    try {
      UUID.fromString(candidate);
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
