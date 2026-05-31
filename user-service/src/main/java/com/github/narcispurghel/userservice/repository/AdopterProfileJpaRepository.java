package com.github.narcispurghel.userservice.repository;

import com.github.narcispurghel.userservice.entity.AdopterProfile;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdopterProfileJpaRepository extends JpaRepository<AdopterProfile, UUID> {
  Optional<AdopterProfile> findByUser_Id(UUID userId);
}
