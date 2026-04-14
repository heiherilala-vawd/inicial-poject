package com.example.demo.endpoint.rest.mapper;

import com.example.demo.client.model.CrupdateUser;
import com.example.demo.client.model.Role;
import com.example.demo.client.model.Sex;
import com.example.demo.client.model.User;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class UserMapper {

  public com.example.demo.model.User toDomain(User restUser) {
    return com.example.demo.model.User.builder()
        .id(restUser.getId())
        .role(
            restUser.getRole() != null
                ? com.example.demo.model.User.Role.valueOf(restUser.getRole().name())
                : null)
        .firstName(restUser.getFirstName())
        .lastName(restUser.getLastName())
        .sex(
            restUser.getSex() != null
                ? com.example.demo.model.User.Sex.valueOf(restUser.getSex().name())
                : null)
        .email(restUser.getEmail())
        .build();
  }

  public com.example.demo.model.User toDomain(CrupdateUser restUser) {
    return com.example.demo.model.User.builder()
        .id(restUser.getId())
        .role(
            restUser.getRole() != null
                ? com.example.demo.model.User.Role.valueOf(restUser.getRole().name())
                : com.example.demo.model.User.Role.EMPLOYEE)
        .firstName(restUser.getFirstName())
        .lastName(restUser.getLastName())
        .sex(
            restUser.getSex() != null
                ? com.example.demo.model.User.Sex.valueOf(restUser.getSex().name())
                : null)
        .email(restUser.getEmail())
        .password(restUser.getPassword())
        .build();
  }

  public User toRestUser(com.example.demo.model.User domainUser) {
    User restUser = new User();
    restUser.setId(domainUser.getId());
    restUser.setRole(
        domainUser.getRole() == null ? null : Role.valueOf(domainUser.getRole().name()));
    restUser.setFirstName(domainUser.getFirstName());
    restUser.setLastName(domainUser.getLastName());
    restUser.setSex(domainUser.getSex() == null ? null : Sex.valueOf(domainUser.getSex().name()));
    restUser.setEmail(domainUser.getEmail());
    restUser.setCreatedAt(domainUser.getCreatedAt());
    restUser.setUpdatedAt(domainUser.getUpdatedAt());
    restUser.setComment(domainUser.getComment());

    if (domainUser.getCreatedBy() != null) {
      restUser.setCreatedBy(domainUser.getCreatedBy().getId());
    }
    if (domainUser.getUpdatedBy() != null) {
      restUser.setUpdatedBy(domainUser.getUpdatedBy().getId());
    }

    return restUser;
  }

  public List<User> toRestUsers(List<com.example.demo.model.User> domainUsers) {
    return domainUsers.stream().map(this::toRestUser).collect(java.util.stream.Collectors.toList());
  }

  public List<com.example.demo.model.User> toDomain(List<User> restUsers) {
    return restUsers.stream().map(this::toDomain).collect(java.util.stream.Collectors.toList());
  }
}
