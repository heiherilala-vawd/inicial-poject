package com.example.demo.Service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.demo.model.BoundedPageSize;
import com.example.demo.model.PageFromOne;
import com.example.demo.model.User;
import com.example.demo.model.exception.NotFoundException;
import com.example.demo.repository.Dao.UserManagerDao;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserRepository repository;

  @Mock private UserManagerDao userManagerDao;

  @InjectMocks private UserService userService;

  private User existingUser;
  private User newUser;
  private String userId;

  @BeforeEach
  void setUp() {
    userId = "123e4567-e89b-12d3-a456-426614174000";

    existingUser =
        User.builder()
            .id(userId)
            .email("john.doe@example.com")
            .firstName("John")
            .lastName("Doe")
            .password("encodedPassword123")
            .role(User.Role.EMPLOYEE)
            .comment("Original comment")
            .build();

    newUser =
        User.builder()
            .email("john.doe@example.com")
            .firstName("John Updated")
            .lastName("Doe Updated")
            .build();
  }

  // ==================== TESTS POUR getById ====================

  @Test
  void getById_ShouldReturnUser_WhenUserExists() {
    // Given
    when(repository.findById(userId)).thenReturn(Optional.of(existingUser));

    // When
    User result = userService.getById(userId);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(userId);
    assertThat(result.getEmail()).isEqualTo("john.doe@example.com");
    verify(repository).findById(userId);
  }

  @Test
  void getById_ShouldThrowNotFoundException_WhenUserDoesNotExist() {
    // Given
    String nonExistentId = "non-existent-id";
    when(repository.findById(nonExistentId)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> userService.getById(nonExistentId))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("User with id " + nonExistentId + " not found");

    verify(repository).findById(nonExistentId);
  }

  // ==================== TESTS POUR getByEmail ====================

  @Test
  void getByEmail_ShouldReturnUserOptional_WhenUserExists() {
    // Given
    String email = "john.doe@example.com";
    when(repository.findByEmail(email)).thenReturn(Optional.of(existingUser));

    // When
    Optional<User> result = userService.getByEmail(email);

    // Then
    assertThat(result).isPresent();
    assertThat(result.get().getEmail()).isEqualTo(email);
    verify(repository).findByEmail(email);
  }

  @Test
  void getByEmail_ShouldReturnEmptyOptional_WhenUserDoesNotExist() {
    // Given
    String email = "nonexistent@example.com";
    when(repository.findByEmail(email)).thenReturn(Optional.empty());

    // When
    Optional<User> result = userService.getByEmail(email);

    // Then
    assertThat(result).isEmpty();
    verify(repository).findByEmail(email);
  }

  // ==================== TESTS POUR getUsers ====================

  @Test
  void getUsers_ShouldReturnFilteredUsers_WhenCriteriaProvided() {
    // Given
    PageFromOne page = new PageFromOne("1");
    BoundedPageSize pageSize = new BoundedPageSize("10");
    String firstName = "John";
    String lastName = "Doe";
    String email = "john";
    User.Role role = User.Role.EMPLOYEE;

    Pageable pageable = PageRequest.of(0, 10);
    List<User> expectedUsers = List.of(existingUser);

    when(userManagerDao.findByCriteria(
            eq(firstName), eq(lastName), eq(email), eq(role), any(Pageable.class)))
        .thenReturn(expectedUsers);

    // When
    List<User> result = userService.getUsers(page, pageSize, firstName, lastName, email, role);

    // Then
    assertThat(result).hasSize(1);
    assertThat(result.get(0)).isEqualTo(existingUser);
    verify(userManagerDao)
        .findByCriteria(eq(firstName), eq(lastName), eq(email), eq(role), any(Pageable.class));
  }

  @Test
  void getUsers_ShouldHandleNullCriteria_WhenNoFiltersProvided() {
    // Given
    PageFromOne page = new PageFromOne("1");
    BoundedPageSize pageSize = new BoundedPageSize("10");
    List<User> expectedUsers = List.of(existingUser);

    when(userManagerDao.findByCriteria(eq(null), eq(null), eq(null), eq(null), any(Pageable.class)))
        .thenReturn(expectedUsers);

    // When
    List<User> result = userService.getUsers(page, pageSize, null, null, null, null);

    // Then
    assertThat(result).hasSize(1);
    verify(userManagerDao)
        .findByCriteria(eq(null), eq(null), eq(null), eq(null), any(Pageable.class));
  }

  // ==================== TESTS POUR deleteById ====================

  @Test
  void deleteById_ShouldDeleteUser_WhenUserExists() {
    // Given
    when(repository.findById(userId)).thenReturn(Optional.of(existingUser));
    doNothing().when(repository).delete(existingUser);

    // When
    userService.deleteById(userId);

    // Then
    verify(repository).findById(userId);
    verify(repository).delete(existingUser);
  }

  @Test
  void deleteById_ShouldThrowNotFoundException_WhenUserDoesNotExist() {
    // Given
    String nonExistentId = "non-existent-id";
    when(repository.findById(nonExistentId)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> userService.deleteById(nonExistentId))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("User with id " + nonExistentId + " not found");

    verify(repository, never()).delete(any());
  }

  // ==================== TESTS POUR getByEmailOrThrow ====================

  @Test
  void getByEmailOrThrow_ShouldReturnUser_WhenUserExists() {
    // Given
    String email = "john.doe@example.com";
    when(repository.findByEmail(email)).thenReturn(Optional.of(existingUser));

    // When
    User result = userService.getByEmailOrThrow(email);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getEmail()).isEqualTo(email);
    verify(repository).findByEmail(email);
  }

  @Test
  void getByEmailOrThrow_ShouldThrowNotFoundException_WhenUserDoesNotExist() {
    // Given
    String email = "nonexistent@example.com";
    when(repository.findByEmail(email)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> userService.getByEmailOrThrow(email))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("User with email " + email + " not found");

    verify(repository).findByEmail(email);
  }
}
