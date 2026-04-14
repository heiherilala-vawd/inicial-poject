package com.example.demo.endpoint.rest.security;

import com.example.demo.model.User;
import com.example.demo.model.exception.BadRequestException;
import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.RequestMatcher;

@AllArgsConstructor
public class SelfMatcher implements RequestMatcher {
  private final UserService userService;
  private final HttpMethod method;
  private final String antPattern;

  private static final Pattern SELFABLE_URI_PATTERN =
      // /resourceType/id/...
      Pattern.compile("/[^/]+/(?<id>[^/]+)(/.*)?");

  @Override
  public boolean matches(HttpServletRequest request) {

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      throw new BadRequestException("User not authenticated");
    }

    String email = authentication.getName();
    User user =
        userService
            .getByEmail(email)
            .orElseThrow(() -> new AccessDeniedException("User not found with email: " + email));

    String userIdFromToken = user.getId();
    String userIdFromURL = getSelfId(request);
    Boolean isMatches = Objects.equals(userIdFromURL, userIdFromToken);
    if (!isMatches) {
      throw new AccessDeniedException(
          "The user with id " + userIdFromToken + " can't update author user");
    }
    return isMatches;
  }

  private String getSelfId(HttpServletRequest request) {
    Matcher uriMatcher = SELFABLE_URI_PATTERN.matcher(request.getRequestURI());
    return uriMatcher.find() ? uriMatcher.group("id") : null;
  }
}
