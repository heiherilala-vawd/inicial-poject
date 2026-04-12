package com.example.demo.endpoint.rest.controller;

import com.example.demo.client.model.CrupdateUser;
import com.example.demo.client.model.User;
import com.example.demo.endpoint.rest.mapper.UserMapper;
import com.example.demo.service.UserService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class UserController {
  private final UserService userService;
  private final UserMapper userMapper;

  @PutMapping(value = "/users")
  public List<User> crupdateUsers(@RequestBody List<CrupdateUser> toWrite) {
    var saved = userService.saveAll(toWrite.stream().map(userMapper::toDomain).toList());
    return saved.stream().map(userMapper::toRestUser).toList();
  }

  @GetMapping("/users/{id}")
  public User getUserById(@PathVariable String id) {
    return userMapper.toRestUser(userService.getById(id));
  }

  /*
    @GetMapping("/users")
    public List<User> getUsers(
        @RequestParam(name = "page", required = false) PageFromOne page,
        @RequestParam(name = "page_size", required = false) BoundedPageSize pageSize,
        @RequestParam(name = "first_name", required = false, defaultValue = "") String firstName,
        @RequestParam(name = "last_name", required = false, defaultValue = "") String lastName,
        @RequestParam(name = "email", required = false, defaultValue = "") String email,
        @RequestParam(name = "role", required = false) school.hei.haapi.endpoint.rest.model.User.Role role) {

      school.hei.haapi.model.User.Role domainRole = role != null
          ? school.hei.haapi.model.User.Role.valueOf(role.name())
          : null;

      return userService.getUsers(page, pageSize, firstName, lastName, email, domainRole).stream()
          .map(userMapper::toRestUser)
          .collect(Collectors.toList());
    }
  ***/
  @DeleteMapping("/users/{id}")
  public void deleteUserById(@PathVariable String id) {
    userService.deleteById(id);
  }
}
