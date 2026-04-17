package com.example.demo.integration;

import static com.example.demo.integration.conf.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

import com.example.demo.SentryConf;
import com.example.demo.client.api.UsersApi;
import com.example.demo.client.invoker.ApiClient;
import com.example.demo.client.model.CrupdateUser;
import com.example.demo.client.model.Role;
import com.example.demo.client.model.Sex;
import com.example.demo.client.model.User;
import com.example.demo.endpoint.rest.security.jwt.JwtUtils;
import com.example.demo.integration.conf.AbstractContextInitializer;
import com.example.demo.integration.conf.TestUtils;
import java.sql.Connection;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ContextConfiguration(initializers = UserIT.ContextInitializer.class)
class UserIT {
  @Autowired private DataSource dataSource;

  @MockitoBean private SentryConf sentryConf;
  @MockitoBean private AuthenticationManager authenticationManagerMock;
  @MockitoBean private JwtUtils jwtServiceMock;

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, ContextInitializer.SERVER_PORT);
  }

  @BeforeEach
  void setUp() throws Exception {
    // Configuration du mock JwtService - ÉQUIVALENT DE setUpCognito
    TestUtils.setUpJwtService(jwtServiceMock);
    TestUtils.setUpAuthenticationManager(authenticationManagerMock);
    try (Connection conn = dataSource.getConnection()) {
      ScriptUtils.executeSqlScript(conn, new ClassPathResource("db/testdata/V99_1__testdata.sql"));
    }
  }

  // =========================
  // TESTS AVEC DIFFÉRENTS RÔLES
  // =========================

  @Test
  void admin_can_get_any_user_by_id() throws Exception {
    ApiClient adminClient = anApiClient(ADMIN_TOKEN);
    UsersApi api = new UsersApi(adminClient);

    User admin = api.getUserById(ADMIN_ID);
    User employee = api.getUserById(EMPLOYEE_ID);
    User warehouse = api.getUserById(WAREHOUSE_ID);

    assertEquals(admin1(), admin);
    assertEquals(employee1(), employee);
    assertEquals(warehouseWorker1(), warehouse);
  }

  @Test
  void employee_can_only_get_own_user_by_id() throws Exception {
    ApiClient employeeClient = anApiClient(EMPLOYEE_TOKEN);
    UsersApi api = new UsersApi(employeeClient);

    // Employee peut voir son propre profil
    User ownUser = api.getUserById(EMPLOYEE_ID);
    assertEquals(employee1(), ownUser);

    // Employee ne peut pas voir les autres utilisateurs
    // todo : make toh forbiden exeption
    assertThrowsApiException(INTERNAL_SERVER_ERROR, () -> api.getUserById(ADMIN_ID));
    assertThrowsApiException(INTERNAL_SERVER_ERROR, () -> api.getUserById(WAREHOUSE_ID));
  }

  @Test
  void user_with_bad_token_cannot_get_any_user() {
    ApiClient badClient = anApiClient(BAD_TOKEN);
    UsersApi api = new UsersApi(badClient);

    assertThrowsForbiddenException(() -> api.getUserById(ADMIN_ID));
  }

  @Test
  void admin_can_get_all_users() throws Exception {
    ApiClient adminClient = anApiClient(ADMIN_TOKEN);
    UsersApi api = new UsersApi(adminClient);

    List<User> users = api.getUsers(1, 100, null, null, null, null);

    assertTrue(users.size() >= 7);
    assertTrue(users.contains(admin1()));
    assertTrue(users.contains(employee1()));
  }

  @Test
  void employee_cannot_get_all_users() {
    ApiClient employeeClient = anApiClient(EMPLOYEE_TOKEN);
    UsersApi api = new UsersApi(employeeClient);

    assertThrowsForbiddenException(() -> api.getUsers(1, 100, null, null, null, null));
  }

  @Test
  @DirtiesContext
  void admin_can_update_users() throws Exception {
    ApiClient adminClient = anApiClient(ADMIN_TOKEN);
    UsersApi api = new UsersApi(adminClient);

    CrupdateUser newUser1 = userToCrupdateUser(admin1());
    CrupdateUser newUser2 = userToCrupdateUser(employee1());
    CrupdateUser newUser3 = userToCrupdateUser(administration1());

    newUser1.setLastName("new last name");
    newUser2.setFirstName("new first name");
    newUser3.setSex(Sex.F);

    User newAdmin1 = admin1();
    newAdmin1.setLastName("new last name");

    List<User> created = api.crupdateUsers(List.of(newUser1, newUser2, newUser3));
    User user =
        created.stream().filter(u -> ADMIN_EMAIL.equals(u.getEmail())).findFirst().orElse(null);
    newAdmin1.setUpdatedAt(user.getUpdatedAt());
    newAdmin1.setUpdatedBy(admin1().getId());

    assertEquals(3, created.size());
    assertNotNull(created.get(0).getId());
    assertTrue(created.contains(newAdmin1));
  }

  @Test
  void employee_cannot_create_users() {
    ApiClient employeeClient = anApiClient(EMPLOYEE_TOKEN);
    UsersApi api = new UsersApi(employeeClient);

    assertThrowsForbiddenException(() -> api.crupdateUsers(List.of(someCreatableUser())));
  }

  /*
   @Test
   void admin_can_delete_user() throws Exception {
     ApiClient adminClient = anApiClient(ADMIN_TOKEN);
     UsersApi api = new UsersApi(adminClient);

     // Créer un utilisateur
     CrupdateUser newUser = someCreatableUser();
     List<User> created = api.crupdateUsers(List.of(newUser));
     String userId = created.get(0).getId();

     // Supprimer l'utilisateur
     api.deleteUserById(userId);

     // Vérifier la suppression
     assertThrowsApiException("404 Not Found", () -> api.getUserById(userId));
   }
  */

  @Test
  void employee_cannot_delete_user() {
    ApiClient employeeClient = anApiClient(EMPLOYEE_TOKEN);
    UsersApi api = new UsersApi(employeeClient);

    assertThrowsForbiddenException(() -> api.deleteUserById(USER1_ID));
  }

  // =========================
  // TESTS AVEC FILTRES
  // =========================

  @Test
  void admin_can_filter_users_by_first_name() throws Exception {
    ApiClient adminClient = anApiClient(ADMIN_TOKEN);
    UsersApi api = new UsersApi(adminClient);

    List<User> users = api.getUsers(1, 100, "Alice", null, null, null);

    assertEquals(1, users.size());
    assertEquals(user1(), users.get(0));
  }

  @Test
  void admin_can_filter_users_by_role() throws Exception {
    ApiClient adminClient = anApiClient(ADMIN_TOKEN);
    UsersApi api = new UsersApi(adminClient);

    List<User> users = api.getUsers(1, 100, null, null, null, Role.EMPLOYEE);

    assertTrue(users.stream().allMatch(u -> u.getRole() == Role.EMPLOYEE));
  }

  // =========================
  // CONTEXT INITIALIZER
  // =========================

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
