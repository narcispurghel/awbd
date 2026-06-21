package com.github.narcispurghel.animalservice.repository;

import com.github.narcispurghel.animalservice.entity.MedicalRecord;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicalRecordJpaRepository extends JpaRepository<MedicalRecord, UUID> {
  List<MedicalRecord> findByAnimalIdOrderByExaminationDateDesc(UUID animalId);

  Page<MedicalRecord> findByAnimalId(UUID animalId, Pageable pageable);
}
