package com.expense.tracker.user.repository;

import com.expense.tracker.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * CONCEPT: Spring Data JPA Repository
 * We don't write a single line of SQL or an implementation class here.
 * By extending JpaRepository<User, Long> (entity type, primary key type),
 * Spring Data JPA generates a full implementation at startup with methods
 * like save(), findById(), findAll(), deleteById()...
 *
 * WHY: This eliminates massive amounts of repetitive DAO boilerplate.
 *
 * CONCEPT: Derived query methods
 * findByEmail(String email) - Spring parses the METHOD NAME itself
 * ("find by Email") and generates the correct SQL/JPQL query automatically.
 * No annotation or implementation needed for straightforward queries like this.
 */
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
