package com.example.demo.endpoint.rest.security;

import static org.springframework.http.HttpMethod.*;

import com.example.demo.endpoint.rest.security.jwt.JwtAuthenticationEntryPoint;
import com.example.demo.endpoint.rest.security.jwt.JwtAuthenticationFilter;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final JwtAuthenticationEntryPoint unauthorizedHandler;
  private final UserService userService;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.cors(cors -> {})
        .csrf(csrf -> csrf.disable())
        .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth
                    // =========================
                    // AUTH & PUBLIC ENDPOINTS
                    // =========================
                    .requestMatchers("/auth/login", "/auth/register", "/ping")
                    .permitAll()
                    .requestMatchers("/auth/whoami")
                    .authenticated()

                    // =========================
                    // USERS
                    // =========================
                    // GET /users, GET /users/{id} - Accessible par ADMIN
                    .requestMatchers(new SelfMatcher(userService, GET, "/users/*"))
                    .authenticated()

                    // PUT /users - Création/Mise à jour d'utilisateurs - ADMIN uniquement
                    .requestMatchers(PUT, "/users")
                    .hasRole("ADMIN")
                    .requestMatchers(new SelfMatcher(userService, PUT, "/users"))
                    .authenticated()

                    // DELETE /users - ADMIN uniquement
                    .requestMatchers(DELETE, "/users/*")
                    .hasRole("ADMIN")

                    // =========================
                    // COMPANY
                    // =========================
                    // GET /companies, GET /companies/{id} - ADMIN et ADMINISTRATION
                    .requestMatchers(GET, "/companies", "/companies/*")
                    .hasAnyRole("ADMIN", "ADMINISTRATION")
                    // PUT /companies - ADMIN et ADMINISTRATION
                    .requestMatchers(PUT, "/companies")
                    .hasAnyRole("ADMIN", "ADMINISTRATION")
                    // DELETE /companies - ADMIN uniquement
                    .requestMatchers(DELETE, "/companies/*")
                    .hasRole("ADMIN")

                    // =========================
                    // JOB
                    // =========================
                    // GET /jobs, GET /jobs/{id} - ADMIN, ADMINISTRATION, WAREHOUSE_WORKER
                    .requestMatchers(GET, "/jobs", "/jobs/*")
                    .hasAnyRole("ADMIN", "ADMINISTRATION", "WAREHOUSE_WORKER")
                    // PUT /jobs - ADMIN et ADMINISTRATION
                    .requestMatchers(PUT, "/jobs")
                    .hasAnyRole("ADMIN", "ADMINISTRATION")
                    // DELETE /jobs - ADMIN uniquement
                    .requestMatchers(DELETE, "/jobs/*")
                    .hasRole("ADMIN")

                    // =========================
                    // WAREHOUSE
                    // =========================
                    // GET /warehouses, GET /warehouses/{id} - ADMIN, ADMINISTRATION,
                    // WAREHOUSE_WORKER
                    .requestMatchers(GET, "/warehouses", "/warehouses/*")
                    .hasAnyRole("ADMIN", "ADMINISTRATION", "WAREHOUSE_WORKER")
                    // PUT /warehouses - ADMIN, ADMINISTRATION, WAREHOUSE_WORKER
                    .requestMatchers(PUT, "/warehouses")
                    .hasAnyRole("ADMIN", "ADMINISTRATION", "WAREHOUSE_WORKER")
                    // DELETE /warehouses - ADMIN uniquement
                    .requestMatchers(DELETE, "/warehouses/*")
                    .hasRole("ADMIN")

                    // =========================
                    // EQUIPMENT
                    // =========================
                    // GET /equipment, GET /equipment/{id} - ADMIN, ADMINISTRATION,
                    // WAREHOUSE_WORKER, EMPLOYEE
                    .requestMatchers(GET, "/equipment", "/equipment/*")
                    .hasAnyRole("ADMIN", "ADMINISTRATION", "WAREHOUSE_WORKER", "EMPLOYEE")
                    // PUT /equipment - ADMIN, ADMINISTRATION, WAREHOUSE_WORKER
                    .requestMatchers(PUT, "/equipment")
                    .hasAnyRole("ADMIN", "ADMINISTRATION", "WAREHOUSE_WORKER")
                    // DELETE /equipment - ADMIN uniquement
                    .requestMatchers(DELETE, "/equipment/*")
                    .hasRole("ADMIN")

                    // =========================
                    // MATERIAL
                    // =========================
                    // GET /materials, GET /materials/{id} - ADMIN, ADMINISTRATION,
                    // WAREHOUSE_WORKER, EMPLOYEE
                    .requestMatchers(GET, "/materials", "/materials/*")
                    .hasAnyRole("ADMIN", "ADMINISTRATION", "WAREHOUSE_WORKER", "EMPLOYEE")
                    // PUT /materials - ADMIN, ADMINISTRATION, WAREHOUSE_WORKER
                    .requestMatchers(PUT, "/materials")
                    .hasAnyRole("ADMIN", "ADMINISTRATION", "WAREHOUSE_WORKER")
                    // DELETE /materials - ADMIN uniquement
                    .requestMatchers(DELETE, "/materials/*")
                    .hasRole("ADMIN")

                    // =========================
                    // MONETARY MOVEMENT
                    // =========================
                    // GET /monetary_movements, GET /monetary_movements/{id} - ADMIN, ADMINISTRATION
                    .requestMatchers(GET, "/monetary_movements", "/monetary_movements/*")
                    .hasAnyRole("ADMIN", "ADMINISTRATION")
                    // PUT /monetary_movements - ADMIN, ADMINISTRATION
                    .requestMatchers(PUT, "/monetary_movements")
                    .hasAnyRole("ADMIN", "ADMINISTRATION")
                    // DELETE /monetary_movements - ADMIN uniquement
                    .requestMatchers(DELETE, "/monetary_movements/*")
                    .hasRole("ADMIN")

                    // =========================
                    // EXPENSE
                    // =========================
                    // GET /expenses, GET /expenses/{id} - ADMIN, ADMINISTRATION
                    .requestMatchers(GET, "/expenses", "/expenses/*")
                    .hasAnyRole("ADMIN", "ADMINISTRATION")
                    // PUT /expenses - ADMIN, ADMINISTRATION
                    .requestMatchers(PUT, "/expenses")
                    .hasAnyRole("ADMIN", "ADMINISTRATION")
                    // DELETE /expenses - ADMIN uniquement
                    .requestMatchers(DELETE, "/expenses/*")
                    .hasRole("ADMIN")

                    // =========================
                    // INCOME
                    // =========================
                    // GET /incomes, GET /incomes/{id} - ADMIN, ADMINISTRATION
                    .requestMatchers(GET, "/incomes", "/incomes/*")
                    .hasAnyRole("ADMIN", "ADMINISTRATION")
                    // PUT /incomes - ADMIN, ADMINISTRATION
                    .requestMatchers(PUT, "/incomes")
                    .hasAnyRole("ADMIN", "ADMINISTRATION")
                    // DELETE /incomes - ADMIN uniquement
                    .requestMatchers(DELETE, "/incomes/*")
                    .hasRole("ADMIN")

                    // =========================
                    // TRAVEL EXPENSE
                    // =========================
                    // GET /travel_expenses, GET /travel_expenses/{id} - ADMIN, ADMINISTRATION
                    .requestMatchers(GET, "/travel_expenses", "/travel_expenses/*")
                    .hasAnyRole("ADMIN", "ADMINISTRATION")
                    // PUT /travel_expenses - ADMIN, ADMINISTRATION
                    .requestMatchers(PUT, "/travel_expenses")
                    .hasAnyRole("ADMIN", "ADMINISTRATION")
                    // DELETE /travel_expenses - ADMIN uniquement
                    .requestMatchers(DELETE, "/travel_expenses/*")
                    .hasRole("ADMIN")

                    // =========================
                    // PURCHASE
                    // =========================
                    // GET /purchases, GET /purchases/{id} - ADMIN, ADMINISTRATION, WAREHOUSE_WORKER
                    .requestMatchers(GET, "/purchases", "/purchases/*")
                    .hasAnyRole("ADMIN", "ADMINISTRATION", "WAREHOUSE_WORKER")
                    // PUT /purchases - ADMIN, ADMINISTRATION
                    .requestMatchers(PUT, "/purchases")
                    .hasAnyRole("ADMIN", "ADMINISTRATION")
                    // DELETE /purchases - ADMIN uniquement
                    .requestMatchers(DELETE, "/purchases/*")
                    .hasRole("ADMIN")

                    // =========================
                    // BANK FEE
                    // =========================
                    // GET /bank_fees, GET /bank_fees/{id} - ADMIN, ADMINISTRATION
                    .requestMatchers(GET, "/bank_fees", "/bank_fees/*")
                    .hasAnyRole("ADMIN", "ADMINISTRATION")
                    // PUT /bank_fees - ADMIN, ADMINISTRATION
                    .requestMatchers(PUT, "/bank_fees")
                    .hasAnyRole("ADMIN", "ADMINISTRATION")
                    // DELETE /bank_fees - ADMIN uniquement
                    .requestMatchers(DELETE, "/bank_fees/*")
                    .hasRole("ADMIN")

                    // =========================
                    // OTHER EXPENSE
                    // =========================
                    // GET /other_expenses, GET /other_expenses/{id} - ADMIN, ADMINISTRATION
                    .requestMatchers(GET, "/other_expenses", "/other_expenses/*")
                    .hasAnyRole("ADMIN", "ADMINISTRATION")
                    // PUT /other_expenses - ADMIN, ADMINISTRATION
                    .requestMatchers(PUT, "/other_expenses")
                    .hasAnyRole("ADMIN", "ADMINISTRATION")
                    // DELETE /other_expenses - ADMIN uniquement
                    .requestMatchers(DELETE, "/other_expenses/*")
                    .hasRole("ADMIN")

                    // =========================
                    // EMPLOYEE PAYMENT
                    // =========================
                    // GET /employee_payments, GET /employee_payments/{id} - ADMIN, ADMINISTRATION
                    .requestMatchers(GET, "/employee_payments", "/employee_payments/*")
                    .hasAnyRole("ADMIN", "ADMINISTRATION")
                    // PUT /employee_payments - ADMIN, ADMINISTRATION
                    .requestMatchers(PUT, "/employee_payments")
                    .hasAnyRole("ADMIN", "ADMINISTRATION")
                    // DELETE /employee_payments - ADMIN uniquement
                    .requestMatchers(DELETE, "/employee_payments/*")
                    .hasRole("ADMIN")

                    // =========================
                    // TRAVEL PEOPLE
                    // =========================
                    // GET /travel_people, GET /travel_people/{id} - ADMIN, ADMINISTRATION
                    .requestMatchers(GET, "/travel_people", "/travel_people/*")
                    .hasAnyRole("ADMIN", "ADMINISTRATION")
                    // PUT /travel_people - ADMIN, ADMINISTRATION
                    .requestMatchers(PUT, "/travel_people")
                    .hasAnyRole("ADMIN", "ADMINISTRATION")
                    // DELETE /travel_people - ADMIN uniquement
                    .requestMatchers(DELETE, "/travel_people/*")
                    .hasRole("ADMIN")

                    // =========================
                    // TRAVEL MATERIALS
                    // =========================
                    // GET /travel_materials, GET /travel_materials/{id} - ADMIN, ADMINISTRATION
                    .requestMatchers(GET, "/travel_materials", "/travel_materials/*")
                    .hasAnyRole("ADMIN", "ADMINISTRATION")
                    // PUT /travel_materials - ADMIN, ADMINISTRATION
                    .requestMatchers(PUT, "/travel_materials")
                    .hasAnyRole("ADMIN", "ADMINISTRATION")
                    // DELETE /travel_materials - ADMIN uniquement
                    .requestMatchers(DELETE, "/travel_materials/*")
                    .hasRole("ADMIN")

                    // =========================
                    // TRAVEL EQUIPMENT
                    // =========================
                    // GET /travel_equipment, GET /travel_equipment/{id} - ADMIN, ADMINISTRATION
                    .requestMatchers(GET, "/travel_equipment", "/travel_equipment/*")
                    .hasAnyRole("ADMIN", "ADMINISTRATION")
                    // PUT /travel_equipment - ADMIN, ADMINISTRATION
                    .requestMatchers(PUT, "/travel_equipment")
                    .hasAnyRole("ADMIN", "ADMINISTRATION")
                    // DELETE /travel_equipment - ADMIN uniquement
                    .requestMatchers(DELETE, "/travel_equipment/*")
                    .hasRole("ADMIN")

                    // =========================
                    // DEFAULT - Toute autre requête nécessite une authentification ADMIN
                    // =========================
                    .anyRequest()
                    .hasRole("ADMIN"));

    http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig)
      throws Exception {
    return authConfig.getAuthenticationManager();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
