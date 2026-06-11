package com.github.narcispurghel.animalservice.repository;

import com.github.narcispurghel.animalservice.entity.AnimalPhoto;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnimalPhotoJpaRepository extends JpaRepository<AnimalPhoto, UUID> {
  List<AnimalPhoto> findByAnimalIdOrderBySortOrderAscCreatedAtAsc(UUID animalId);
}
