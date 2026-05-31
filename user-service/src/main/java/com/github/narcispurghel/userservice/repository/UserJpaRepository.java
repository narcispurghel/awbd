package com.github.narcispurghel.userservice.repository;

import com.github.narcispurghel.userservice.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserJpaRepository extends JpaRepository<User, UUID> {
  Optional<User> findByEmail(String email);

  @Query("SELECT u FROM User u LEFT JOIN FETCH u.profile WHERE u.id = :id")
  Optional<User> findByIdWithProfile(@Param("id") UUID id);
}
