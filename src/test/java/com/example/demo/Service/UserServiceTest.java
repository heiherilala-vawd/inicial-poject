package com.example.demo.Service;

import com.example.demo.model.BoundedPageSize;
import com.example.demo.model.PageFromOne;
import com.example.demo.model.User;
import com.example.demo.model.exception.BadRequestException;
import com.example.demo.model.exception.NotFoundException;
import com.example.demo.repository.Dao.UserManagerDao;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;

    @Mock
    private UserManagerDao userManagerDao;

    @InjectMocks
    private UserService userService;

    private User existingUser;
    private User newUser;
    private String userId;

    @BeforeEach
    void setUp() {
        userId = "123e4567-e89b-12d3-a456-426614174000";
        
        existingUser = User.builder()
                .id(userId)
                .email("john.doe@example.com")
                .firstName("John")
                .lastName("Doe")
                .password("encodedPassword123")
                .role(User.Role.EMPLOYEE)
                .comment("Original comment")
                .build();

        newUser = User.builder()
                .email("john.doe@example.com")
                .firstName("John Updated")
                .lastName("Doe Updated")
                .build();
    }

    // ==================== TESTS POUR saveAll ====================

    @Test
    void saveAll_ShouldUpdateExistingUsers_WhenUsersExist() {
        // Given
        List<User> usersToUpdate = List.of(newUser);
        when(repository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(existingUser));
        when(repository.saveAll(anyList())).thenReturn(List.of(existingUser));

        // When
        List<User> result = userService.saveAll(usersToUpdate);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(existingUser.getId());
        assertThat(result.get(0).getPassword()).isEqualTo(existingUser.getPassword());
        verify(repository).findByEmail("john.doe@example.com");
        verify(repository).saveAll(anyList());
    }

    @Test
    void saveAll_ShouldThrowBadRequestException_WhenUserNotFound() {
        // Given
        List<User> usersToUpdate = List.of(newUser);
        when(repository.findByEmail("john.doe@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.saveAll(usersToUpdate))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("User not found with email: john.doe@example.com");
        
        verify(repository, never()).saveAll(anyList());
    }

    @Test
    void saveAll_ShouldHandleMultipleUsers_WhenAllExist() {
        // Given
        User user1 = User.builder().email("user1@example.com").build();
        User user2 = User.builder().email("user2@example.com").build();
        List<User> usersToUpdate = List.of(user1, user2);
        
        User existingUser1 = User.builder().id("1").email("user1@example.com").password("pass1").build();
        User existingUser2 = User.builder().id("2").email("user2@example.com").password("pass2").build();
        
        when(repository.findByEmail("user1@example.com")).thenReturn(Optional.of(existingUser1));
        when(repository.findByEmail("user2@example.com")).thenReturn(Optional.of(existingUser2));
        when(repository.saveAll(anyList())).thenReturn(List.of(existingUser1, existingUser2));

        // When
        List<User> result = userService.saveAll(usersToUpdate);

        // Then
        assertThat(result).hasSize(2);
        verify(repository).saveAll(argThat(list -> ((List<User>) list).size() == 2));
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
        
        when(userManagerDao.findByCriteria(eq(firstName), eq(lastName), eq(email), eq(role), any(Pageable.class)))
                .thenReturn(expectedUsers);

        // When
        List<User> result = userService.getUsers(page, pageSize, firstName, lastName, email, role);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(existingUser);
        verify(userManagerDao).findByCriteria(eq(firstName), eq(lastName), eq(email), eq(role), any(Pageable.class));
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
        verify(userManagerDao).findByCriteria(eq(null), eq(null), eq(null), eq(null), any(Pageable.class));
    }

    // ==================== TESTS POUR updateUser ====================

    @Test
    void updateUser_ShouldUpdateAllFields_WhenAllFieldsProvided() {
        // Given
        User updatedUser = User.builder()
                .firstName("Jane")
                .lastName("Smith")
                .sex(User.Sex.F)
                .email("jane.smith@example.com")
                .role(User.Role.ADMIN)
                .comment("Updated comment")
                .build();
        
        when(repository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(repository.findByEmail("jane.smith@example.com")).thenReturn(Optional.empty());
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User result = userService.updateUser(userId, updatedUser);

        // Then
        assertThat(result.getFirstName()).isEqualTo("Jane");
        assertThat(result.getLastName()).isEqualTo("Smith");
        assertThat(result.getSex()).isEqualTo(User.Sex.F);
        assertThat(result.getEmail()).isEqualTo("jane.smith@example.com");
        assertThat(result.getRole()).isEqualTo(User.Role.ADMIN);
        assertThat(result.getComment()).isEqualTo("Updated comment");
        verify(repository).save(existingUser);
    }

    @Test
    void updateUser_ShouldUpdateOnlyNonNullFields_WhenPartialUpdate() {
        // Given
        User updatedUser = User.builder()
                .firstName("Jane Only")
                .comment("New comment")
                .build();
        
        when(repository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User result = userService.updateUser(userId, updatedUser);

        // Then
        assertThat(result.getFirstName()).isEqualTo("Jane Only");
        assertThat(result.getLastName()).isEqualTo("Doe"); // Not changed
        assertThat(result.getEmail()).isEqualTo("john.doe@example.com"); // Not changed
        assertThat(result.getComment()).isEqualTo("New comment");
        verify(repository).save(existingUser);
    }

    @Test
    void updateUser_ShouldThrowBadRequestException_WhenEmailAlreadyUsedByAnotherUser() {
        // Given
        User updatedUser = User.builder().email("taken@example.com").build();
        User anotherUser = User.builder().id("different-id").email("taken@example.com").build();
        
        when(repository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(repository.findByEmail("taken@example.com")).thenReturn(Optional.of(anotherUser));

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(userId, updatedUser))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Email taken@example.com is already used");
        
        verify(repository, never()).save(any());
    }

    @Test
    void updateUser_ShouldAllowSameEmail_WhenUpdatingSameUser() {
        // Given
        User updatedUser = User.builder().email("john.doe@example.com").build();
        
        when(repository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(repository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(existingUser));
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User result = userService.updateUser(userId, updatedUser);

        // Then
        assertThat(result.getEmail()).isEqualTo("john.doe@example.com");
        verify(repository).save(existingUser);
    }

    @Test
    void updateUser_ShouldThrowNotFoundException_WhenUserToUpdateDoesNotExist() {
        // Given
        String nonExistentId = "non-existent-id";
        when(repository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(nonExistentId, newUser))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User with id " + nonExistentId + " not found");
        
        verify(repository, never()).save(any());
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