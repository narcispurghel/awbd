package com.github.narcispurghel.animalservice.repository;

import com.github.narcispurghel.animalservice.entity.Animal;
import com.github.narcispurghel.animalservice.entity.AnimalStatus;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AnimalJpaRepository
  extends JpaRepository<Animal, UUID>, JpaSpecificationExecutor<Animal> {
  List<Animal> findAllByOrderByNameAsc();
  List<Animal> findByStatusOrderByNameAsc(AnimalStatus status);
  List<Animal> findBySpeciesIdOrderByNameAsc(UUID speciesId);
  List<Animal> findByShelterIdOrderByNameAsc(UUID shelterId);
  List<Animal> findByStatusAndSpeciesIdOrderByNameAsc(AnimalStatus status, UUID speciesId);
  List<Animal> findByStatusAndShelterIdOrderByNameAsc(AnimalStatus status, UUID shelterId);
  List<Animal> findBySpeciesIdAndShelterIdOrderByNameAsc(UUID speciesId, UUID shelterId);
  List<Animal> findByStatusAndSpeciesIdAndShelterIdOrderByNameAsc(
    AnimalStatus status,
    UUID speciesId,
    UUID shelterId
  );

  boolean existsByShelterId(UUID shelterId);
  boolean existsBySpeciesId(UUID speciesId);
  boolean existsByBreedId(UUID breedId);
  boolean existsByTagsId(UUID tagId);

  default List<Animal> filter(
    @Nullable AnimalStatus status,
    @Nullable UUID speciesId,
    @Nullable UUID shelterId
  ) {
    if (status != null && speciesId != null && shelterId != null) {
      return findByStatusAndSpeciesIdAndShelterIdOrderByNameAsc(status, speciesId, shelterId);
    }
    if (status != null && speciesId != null) {
      return findByStatusAndSpeciesIdOrderByNameAsc(status, speciesId);
    }
    if (status != null && shelterId != null) {
      return findByStatusAndShelterIdOrderByNameAsc(status, shelterId);
    }
    if (speciesId != null && shelterId != null) {
      return findBySpeciesIdAndShelterIdOrderByNameAsc(speciesId, shelterId);
    }
    if (status != null) {
      return findByStatusOrderByNameAsc(status);
    }
    if (speciesId != null) {
      return findBySpeciesIdOrderByNameAsc(speciesId);
    }
    if (shelterId != null) {
      return findByShelterIdOrderByNameAsc(shelterId);
    }
    return findAllByOrderByNameAsc();
  }
}
