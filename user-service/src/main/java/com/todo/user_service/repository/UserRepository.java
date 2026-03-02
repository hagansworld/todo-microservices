package com.todo.user_service.repository;

import com.todo.user_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User>findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    Optional<User>findById(UUID id);
    Optional<User> findByVerificationCode(String verificationCode);
    Optional<User>findByPasswordResetToken(String passwordResetToken);


    /*
  Look inside the user table if the keyword matches the first name, lastname,
  email or phone number, return those users
   */
    @Query("SELECT u FROM User u " +
            "WHERE LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(u.phoneNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))" +
            "  OR LOWER(u.role) LIKE LOWER(CONCAT('%', :keyword, '%'))"
    )

    List<User>searchUsers(@Param("keyword") String Keyword);

}
