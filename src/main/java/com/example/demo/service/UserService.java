package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {
  private final UserRepository repository;

  // private final UserManagerDao UserManagerDao;

  public List<User> saveAll(List<User> users) {
    return repository.saveAll(users);
  }

  public User getById(String UserId) {
    return repository.findById(UserId).orElseThrow();
    // .orElseThrow(() -> new ResourceNotFoundException("User with id " + UserId + " not found"));
  }

  public Optional<User> getByEmail(String email) {
    return repository.findByEmail(email);
  }

  /*
    public List<User> getUsers(PageFromOne page, BoundedPageSize pageSize,
                               String firstName, String lastName, String email, User.Role role) {
      if (page == null) {
        page = new PageFromOne(1);
      }
      if (pageSize == null) {
        pageSize = new BoundedPageSize(15);
      }
      Pageable pageable = PageRequest.of(page.getValue() - 1, pageSize.getValue());

      return UserManagerDao.findByCriteria(firstName, lastName, email, role, pageable);
    }
  */
  public User updateUser(String UserId, User updatedUser) {
    User existingUser = getById(UserId);

    if (updatedUser.getFirstName() != null) {
      existingUser.setFirstName(updatedUser.getFirstName());
    }
    if (updatedUser.getLastName() != null) {
      existingUser.setLastName(updatedUser.getLastName());
    }
    if (updatedUser.getSex() != null) {
      existingUser.setSex(updatedUser.getSex());
    }
    if (updatedUser.getEmail() != null) {
      // Check if email is already used by another User
      Optional<User> UserWithEmail = getByEmail(updatedUser.getEmail());
      /*
      if (UserWithEmail.isPresent() && !UserWithEmail.get().getId().equals(UserId)) {
        throw new BadRequestException("Email " + updatedUser.getEmail() + " is already used");
      }
      */
      existingUser.setEmail(updatedUser.getEmail());
    }
    if (updatedUser.getRole() != null) {
      existingUser.setRole(updatedUser.getRole());
    }
    if (updatedUser.getComment() != null) {
      existingUser.setComment(updatedUser.getComment());
    }

    return repository.save(existingUser);
  }

  public void deleteById(String UserId) {
    User User = getById(UserId);
    repository.delete(User);
  }

  public User getByEmailOrThrow(String email) {
    return getByEmail(email).orElseThrow();
    // .orElseThrow(() -> new ResourceNotFoundException("User with email " + email + " not found"));
  }
}
