package com.example.demo.endpoint.rest.security.controller;

import com.example.demo.client.model.AuthResponse;
import com.example.demo.client.model.CrupdateUser;
import com.example.demo.client.model.LoginRequest;
import com.example.demo.endpoint.rest.mapper.UserMapper;
import com.example.demo.endpoint.rest.security.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;
  private final UserMapper userMapper;

  @PostMapping("/login")
  public AuthResponse authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
    return authService.authenticateUser(loginRequest);
  }

  @PostMapping("/register")
  public AuthResponse registerUser(@Valid @RequestBody CrupdateUser user) {
    com.example.demo.model.User modelUser = userMapper.toDomain(user);
    String noEncodedPassword = user.getPassword();

    return authService.registerUser(modelUser, noEncodedPassword);
  }

  @GetMapping("/whoami")
  public AuthResponse whoami() {
    return authService.whoami();
  }
}
