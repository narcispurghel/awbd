package com.github.narcispurghel.animalservice.repository;

import com.github.narcispurghel.animalservice.entity.Breed;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BreedJpaRepository extends JpaRepository<Breed, UUID> {
  List<Breed> findBySpeciesIdOrderByNameAsc(UUID speciesId);
}
