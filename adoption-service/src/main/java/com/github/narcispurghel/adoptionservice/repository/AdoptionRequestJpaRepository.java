package com.github.narcispurghel.adoptionservice.repository;

import com.github.narcispurghel.adoptionservice.entity.AdoptionRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AdoptionRequestJpaRepository
  extends JpaRepository<AdoptionRequest, java.util.UUID>, JpaSpecificationExecutor<AdoptionRequest> {}
