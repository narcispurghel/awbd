package com.github.narcispurghel.animalservice.repository;

import com.github.narcispurghel.animalservice.entity.Shelter;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShelterJpaRepository extends JpaRepository<Shelter, UUID> {}
