package com.example.demo.service;

import com.example.demo.model.BoundedPageSize;
import com.example.demo.model.PageFromOne;
import com.example.demo.model.User;
import com.example.demo.model.exception.BadRequestException;
import com.example.demo.model.exception.NotFoundException;
import com.example.demo.repository.Dao.UserManagerDao;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.utils.PageUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {
  private final UserRepository repository;
  private final UserManagerDao UserManagerDao;

  public List<User> saveAll(List<User> users) {

    List<User> usersToSave = new ArrayList<>();

    for (User user : users) {

      User existingUser =
          repository
              .findByEmail(user.getEmail())
              .orElseThrow(
                  () -> new BadRequestException("User not found with email: " + user.getEmail()));

      user.setPassword(existingUser.getPassword());
      user.setId(existingUser.getId());

      usersToSave.add(user);
    }

    return repository.saveAll(usersToSave);
  }

  public User getById(String UserId) {
    return repository
        .findById(UserId)
        .orElseThrow(() -> new NotFoundException("User with id " + UserId + " not found"));
  }

  public Optional<User> getByEmail(String email) {
    return repository.findByEmail(email);
  }

  public List<User> getUsers(
      PageFromOne page,
      BoundedPageSize pageSize,
      String firstName,
      String lastName,
      String email,
      User.Role role) {
    Pageable pageable = PageUtils.createPageable(page, pageSize);

    return UserManagerDao.findByCriteria(firstName, lastName, email, role, pageable);
  }

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

      if (UserWithEmail.isPresent() && !UserWithEmail.get().getId().equals(UserId)) {
        throw new BadRequestException("Email " + updatedUser.getEmail() + " is already used");
      }

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
    return getByEmail(email)
        .orElseThrow(() -> new NotFoundException("User with email " + email + " not found"));
  }
}
