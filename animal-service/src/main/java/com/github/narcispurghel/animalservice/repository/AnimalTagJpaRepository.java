package com.github.narcispurghel.animalservice.repository;

import com.github.narcispurghel.animalservice.entity.AnimalTag;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnimalTagJpaRepository extends JpaRepository<AnimalTag, UUID> {
  List<AnimalTag> findAllByOrderByNameAsc();
}
