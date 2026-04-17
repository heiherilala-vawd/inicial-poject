package com.example.demo.service.utils;

import com.example.demo.model.CreatAndUpdateEntity;
import com.example.demo.model.User;
import com.example.demo.model.exception.BadRequestException;
import com.example.demo.model.exception.NotFoundException;
import com.example.demo.repository.UserRepository;
import java.time.Instant;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ModificationUtils {
  UserRepository userRepository;

  public void createOrUpdateModel(
      CreatAndUpdateEntity creatAndUpdateToChange,
      CreatAndUpdateEntity creatAndUpdateInDB,
      User crater) {
    if (creatAndUpdateInDB == null) {
      creatAndUpdateToChange.setCreatedAt(Instant.now());
      creatAndUpdateToChange.setCreatedBy(crater);
    } else {
      creatAndUpdateToChange.setCreatedAt(creatAndUpdateInDB.getCreatedAt());
      creatAndUpdateToChange.setCreatedBy(creatAndUpdateInDB.getCreatedBy());
    }
    creatAndUpdateToChange.setUpdatedAt(Instant.now());
    creatAndUpdateToChange.setUpdatedBy(crater);
  }

  public User takePrimaryUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      throw new BadRequestException("User not authenticated");
    }

    String email = authentication.getName();
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new NotFoundException("User not found with email: " + email));

    return user;
  }
}
