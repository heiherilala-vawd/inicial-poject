package com.example.demo.service;

import com.example.demo.model.BoundedPageSize;
import com.example.demo.model.PageFromOne;
import com.example.demo.model.User;
import com.example.demo.model.exception.ForbiddenException;
import com.example.demo.model.exception.NotFoundException;
import com.example.demo.repository.Dao.UserManagerDao;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.utils.ModificationUtils;
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
  private final ModificationUtils modificationUtils;

  public List<User> saveAll(List<User> users) {

    List<User> usersToSave = new ArrayList<>();

    for (User user : users) {

      User existingUser = repository.findByEmail(user.getEmail()).orElse(null);
      if (existingUser != null) {
        user.setPassword(existingUser.getPassword());
        user.setId(existingUser.getId());
      } else {
        throw new ForbiddenException("That endpoint can not create.");
      }
      modificationUtils.createOrUpdateModel(
          user, existingUser, modificationUtils.takePrimaryUser());
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

  public void deleteById(String UserId) {
    User User = getById(UserId);
    repository.delete(User);
  }

  public User getByEmailOrThrow(String email) {
    return getByEmail(email)
        .orElseThrow(() -> new NotFoundException("User with email " + email + " not found"));
  }
}
