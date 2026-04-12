package com.example.demo.repository.Dao;

import com.example.demo.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class UserManagerDao {
  private final EntityManager entityManager;

  public List<User> findByCriteria(
      String firstName, String lastName, String email, User.Role role, Pageable pageable) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<User> query = cb.createQuery(User.class);
    Root<User> user = query.from(User.class);

    List<Predicate> predicates = new ArrayList<>();

    if (firstName != null && !firstName.isEmpty()) {
      predicates.add(cb.like(cb.lower(user.get("firstName")), "%" + firstName.toLowerCase() + "%"));
    }
    if (lastName != null && !lastName.isEmpty()) {
      predicates.add(cb.like(cb.lower(user.get("lastName")), "%" + lastName.toLowerCase() + "%"));
    }
    if (email != null && !email.isEmpty()) {
      predicates.add(cb.like(cb.lower(user.get("email")), "%" + email.toLowerCase() + "%"));
    }
    if (role != null) {
      predicates.add(cb.equal(user.get("role"), role));
    }

    query.where(predicates.toArray(new Predicate[0]));
    query.orderBy(cb.asc(user.get("firstName")), cb.asc(user.get("lastName")));

    TypedQuery<User> typedQuery = entityManager.createQuery(query);
    typedQuery.setFirstResult((int) pageable.getOffset());
    typedQuery.setMaxResults(pageable.getPageSize());

    return typedQuery.getResultList();
  }

  public long countByCriteria(String firstName, String lastName, String email, User.Role role) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<Long> query = cb.createQuery(Long.class);
    Root<User> user = query.from(User.class);

    List<Predicate> predicates = new ArrayList<>();

    if (firstName != null && !firstName.isEmpty()) {
      predicates.add(cb.like(cb.lower(user.get("firstName")), "%" + firstName.toLowerCase() + "%"));
    }
    if (lastName != null && !lastName.isEmpty()) {
      predicates.add(cb.like(cb.lower(user.get("lastName")), "%" + lastName.toLowerCase() + "%"));
    }
    if (email != null && !email.isEmpty()) {
      predicates.add(cb.like(cb.lower(user.get("email")), "%" + email.toLowerCase() + "%"));
    }
    if (role != null) {
      predicates.add(cb.equal(user.get("role"), role));
    }

    query.select(cb.count(user));
    query.where(predicates.toArray(new Predicate[0]));

    return entityManager.createQuery(query).getSingleResult();
  }
}
