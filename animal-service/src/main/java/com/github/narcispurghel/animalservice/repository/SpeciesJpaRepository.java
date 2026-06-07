package com.github.narcispurghel.animalservice.repository;

import com.github.narcispurghel.animalservice.entity.Species;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpeciesJpaRepository extends JpaRepository<Species, UUID> {}
