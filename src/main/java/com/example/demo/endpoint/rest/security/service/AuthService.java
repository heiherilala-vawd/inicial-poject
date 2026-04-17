package com.example.demo.endpoint.rest.security.service;

import com.example.demo.client.model.AuthResponse;
import com.example.demo.client.model.LoginRequest;
import com.example.demo.endpoint.rest.security.jwt.JwtUtils;
import com.example.demo.model.User;
import com.example.demo.model.exception.BadRequestException;
import com.example.demo.model.exception.NotFoundException;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import com.example.demo.service.utils.ModificationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
  private final AuthenticationManager authenticationManager;
  private final JwtUtils jwtUtils;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserService userService;
  private final ModificationUtils modificationUtils;

  public AuthResponse authenticateUser(LoginRequest loginRequest) {

    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
        new UsernamePasswordAuthenticationToken(
            loginRequest.getEmail(), loginRequest.getPassword());
    Authentication authentication =
        authenticationManager.authenticate(usernamePasswordAuthenticationToken);

    SecurityContextHolder.getContext().setAuthentication(authentication);
    String jwt = jwtUtils.generateJwtToken(authentication);

    User user =
        userRepository
            .findByEmail(loginRequest.getEmail())
            .orElseThrow(() -> new BadRequestException("User not found"));

    AuthResponse authResponse = new AuthResponse();
    authResponse.setEmail(user.getEmail());
    authResponse.setId(user.getId());
    authResponse.setType("Bearer");
    authResponse.setRole(user.getRole().name());
    authResponse.setToken(jwt);

    return authResponse;
  }

  public AuthResponse registerUser(User user, String noEncodedPassword) {
    if (userRepository.findByEmail(user.getEmail()).isPresent()) {
      throw new BadRequestException("Email is already in use");
    }
    user.setPassword(passwordEncoder.encode(user.getPassword()));

    modificationUtils.createOrUpdateModel(user, null, null);
    User savedUser = userRepository.save(user);
    modificationUtils.createOrUpdateModel(savedUser, null, savedUser);
    savedUser = userRepository.save(user);

    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(savedUser.getEmail(), noEncodedPassword));
    SecurityContextHolder.getContext().setAuthentication(authentication);
    String jwt = jwtUtils.generateJwtToken(authentication);
    AuthResponse authResponse = new AuthResponse();
    authResponse.setEmail(savedUser.getEmail());
    authResponse.setId(savedUser.getId());
    authResponse.setType("Bearer");
    authResponse.setRole(savedUser.getRole().name());
    authResponse.setToken(jwt);

    return authResponse;
  }

  public AuthResponse whoami(String token) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      throw new BadRequestException("User not authenticated");
    }

    String email = authentication.getName();
    User user =
        userService
            .getByEmail(email)
            .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
    AuthResponse authResponse = new AuthResponse();
    authResponse.setEmail(user.getEmail());
    authResponse.setId(user.getId());
    authResponse.setType("Bearer");
    authResponse.setRole(user.getRole().name());
    authResponse.setToken(token);
    return authResponse;
  }
}
