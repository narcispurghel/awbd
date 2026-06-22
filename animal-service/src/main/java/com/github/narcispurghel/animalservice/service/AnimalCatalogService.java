package com.github.narcispurghel.animalservice.service;

import com.github.narcispurghel.animalservice.dto.AnimalDtos;
import com.github.narcispurghel.animalservice.entity.Animal;
import com.github.narcispurghel.animalservice.entity.AnimalStatus;
import com.github.narcispurghel.animalservice.entity.AnimalTag;
import com.github.narcispurghel.animalservice.entity.Breed;
import com.github.narcispurghel.animalservice.entity.MedicalRecord;
import com.github.narcispurghel.animalservice.entity.Shelter;
import com.github.narcispurghel.animalservice.entity.Species;
import com.github.narcispurghel.animalservice.repository.AnimalJpaRepository;
import com.github.narcispurghel.animalservice.repository.BreedJpaRepository;
import com.github.narcispurghel.animalservice.repository.MedicalRecordJpaRepository;
import com.github.narcispurghel.animalservice.repository.ShelterJpaRepository;
import com.github.narcispurghel.animalservice.repository.SpeciesJpaRepository;
import com.github.narcispurghel.animalservice.repository.AnimalTagJpaRepository;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class AnimalCatalogService {

  private final ShelterJpaRepository shelterRepository;
  private final SpeciesJpaRepository speciesRepository;
  private final BreedJpaRepository breedRepository;
  private final AnimalTagJpaRepository tagRepository;
  private final AnimalJpaRepository animalRepository;
  private final MedicalRecordJpaRepository medicalRecordRepository;
  private final AnimalPhotoService animalPhotoService;

  public AnimalCatalogService(
    ShelterJpaRepository shelterRepository,
    SpeciesJpaRepository speciesRepository,
    BreedJpaRepository breedRepository,
    AnimalTagJpaRepository tagRepository,
    AnimalJpaRepository animalRepository,
    MedicalRecordJpaRepository medicalRecordRepository,
    AnimalPhotoService animalPhotoService
  ) {
    this.shelterRepository = shelterRepository;
    this.speciesRepository = speciesRepository;
    this.breedRepository = breedRepository;
    this.tagRepository = tagRepository;
    this.animalRepository = animalRepository;
    this.medicalRecordRepository = medicalRecordRepository;
    this.animalPhotoService = animalPhotoService;
  }

  @Transactional(readOnly = true)
  public List<AnimalDtos.ShelterView> shelters() {
    return shelterRepository.findAll().stream().map(this::toShelterView).toList();
  }

  public AnimalDtos.ShelterView createShelter(AnimalDtos.UpsertShelterRequest body) {
    Shelter shelter = new Shelter();
    applyShelter(shelter, body);
    return toShelterView(shelterRepository.save(shelter));
  }

  public AnimalDtos.ShelterView updateShelter(UUID id, AnimalDtos.UpsertShelterRequest body) {
    Shelter shelter = requireShelter(id);
    applyShelter(shelter, body);
    return toShelterView(shelter);
  }

  public void deleteShelter(UUID id) {
    Shelter shelter = requireShelter(id);
    if (animalRepository.existsByShelterId(id)) {
      throw conflict("Shelter still has animals and cannot be deleted");
    }
    shelterRepository.delete(shelter);
  }

  @Transactional(readOnly = true)
  public List<AnimalDtos.SpeciesView> species() {
    return speciesRepository.findAll().stream().map(this::toSpeciesView).toList();
  }

  public AnimalDtos.SpeciesView createSpecies(AnimalDtos.UpsertSpeciesRequest body) {
    Species species = new Species();
    species.setName(normalizeName(body.name()));
    return toSpeciesView(speciesRepository.save(species));
  }

  public AnimalDtos.SpeciesView updateSpecies(UUID id, AnimalDtos.UpsertSpeciesRequest body) {
    Species species = requireSpecies(id);
    species.setName(normalizeName(body.name()));
    return toSpeciesView(species);
  }

  public void deleteSpecies(UUID id) {
    Species species = requireSpecies(id);
    if (breedRepository.existsBySpeciesId(id)) {
      throw conflict("Species still has breeds and cannot be deleted");
    }
    if (animalRepository.existsBySpeciesId(id)) {
      throw conflict("Species still has animals and cannot be deleted");
    }
    speciesRepository.delete(species);
  }

  @Transactional(readOnly = true)
  public List<AnimalDtos.BreedView> breeds(@Nullable UUID speciesId) {
    List<Breed> breeds = speciesId == null
      ? breedRepository.findAll()
      : breedRepository.findBySpeciesIdOrderByNameAsc(speciesId);
    return breeds.stream().map(this::toBreedView).toList();
  }

  public AnimalDtos.BreedView createBreed(AnimalDtos.UpsertBreedRequest body) {
    Breed breed = new Breed();
    applyBreed(breed, body);
    return toBreedView(breedRepository.save(breed));
  }

  public AnimalDtos.BreedView updateBreed(UUID id, AnimalDtos.UpsertBreedRequest body) {
    Breed breed = requireBreed(id);
    applyBreed(breed, body);
    return toBreedView(breed);
  }

  public void deleteBreed(UUID id) {
    Breed breed = requireBreed(id);
    if (animalRepository.existsByBreedId(id)) {
      throw conflict("Breed still has animals and cannot be deleted");
    }
    breedRepository.delete(breed);
  }

  @Transactional(readOnly = true)
  public List<AnimalDtos.TagView> tags() {
    return tagRepository.findAllByOrderByNameAsc().stream().map(this::toTagView).toList();
  }

  public AnimalDtos.TagView createTag(AnimalDtos.UpsertTagRequest body) {
    AnimalTag tag = new AnimalTag();
    tag.setName(normalizeName(body.name()));
    return toTagView(tagRepository.save(tag));
  }

  public AnimalDtos.TagView updateTag(UUID id, AnimalDtos.UpsertTagRequest body) {
    AnimalTag tag = requireTag(id);
    tag.setName(normalizeName(body.name()));
    return toTagView(tag);
  }

  public void deleteTag(UUID id) {
    AnimalTag tag = requireTag(id);
    if (animalRepository.existsByTagsId(id)) {
      throw conflict("Tag is still assigned to animals and cannot be deleted");
    }
    tagRepository.delete(tag);
  }

  @Transactional(readOnly = true)
  public List<AnimalDtos.AnimalSummary> animals(
    @Nullable AnimalStatus status,
    @Nullable UUID speciesId,
    @Nullable UUID shelterId
  ) {
    return animalRepository
      .filter(status, speciesId, shelterId)
      .stream()
      .map(this::toAnimalSummary)
      .toList();
  }

  @Transactional(readOnly = true)
  public Page<AnimalDtos.AnimalSummary> animals(
    @Nullable AnimalStatus status,
    @Nullable UUID speciesId,
    @Nullable UUID shelterId,
    Pageable pageable
  ) {
    return animalRepository
      .findAll(animalFilter(status, speciesId, shelterId), pageable)
      .map(this::toAnimalSummary);
  }

  private static Specification<Animal> animalFilter(
    @Nullable AnimalStatus status,
    @Nullable UUID speciesId,
    @Nullable UUID shelterId
  ) {
    Specification<Animal> spec = (root, query, cb) -> cb.conjunction();
    if (status != null) {
      spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
    }
    if (speciesId != null) {
      spec = spec.and((root, query, cb) -> cb.equal(root.get("species").get("id"), speciesId));
    }
    if (shelterId != null) {
      spec = spec.and((root, query, cb) -> cb.equal(root.get("shelter").get("id"), shelterId));
    }
    return spec;
  }

  @Transactional(readOnly = true)
  public AnimalDtos.AnimalView animal(UUID id) {
    return toAnimalView(requireAnimal(id));
  }

  public AnimalDtos.AnimalView createAnimal(AnimalDtos.UpsertAnimalRequest body) {
    Animal animal = new Animal();
    applyAnimal(animal, body);
    return toAnimalView(animalRepository.save(animal));
  }

  public AnimalDtos.AnimalView updateAnimal(UUID id, AnimalDtos.UpsertAnimalRequest body) {
    Animal animal = requireAnimal(id);
    applyAnimal(animal, body);
    return toAnimalView(animal);
  }

  public void deleteAnimal(UUID id) {
    Animal animal = requireAnimal(id);
    animalPhotoService.deleteAllForAnimal(id);
    medicalRecordRepository.deleteAll(
      medicalRecordRepository.findByAnimalIdOrderByExaminationDateDesc(id)
    );
    animalRepository.delete(animal);
  }

  @Transactional(readOnly = true)
  public List<AnimalDtos.MedicalRecordView> medicalRecords(UUID animalId) {
    requireAnimal(animalId);
    return medicalRecordRepository
      .findByAnimalIdOrderByExaminationDateDesc(animalId)
      .stream()
      .map(this::toMedicalRecordView)
      .toList();
  }

  @Transactional(readOnly = true)
  public Page<AnimalDtos.MedicalRecordView> medicalRecords(UUID animalId, Pageable pageable) {
    requireAnimal(animalId);
    return medicalRecordRepository
      .findByAnimalId(animalId, pageable)
      .map(this::toMedicalRecordView);
  }

  public AnimalDtos.MedicalRecordView addMedicalRecord(
    UUID animalId,
    AnimalDtos.UpsertMedicalRecordRequest body
  ) {
    MedicalRecord record = new MedicalRecord();
    record.setAnimal(requireAnimal(animalId));
    applyMedicalRecord(record, body);
    return toMedicalRecordView(medicalRecordRepository.save(record));
  }

  @Transactional(readOnly = true)
  public AnimalDtos.MedicalRecordView medicalRecord(UUID animalId, UUID recordId) {
    requireAnimal(animalId);
    MedicalRecord record = requireMedicalRecord(recordId);
    if (!record.getAnimal().getId().equals(animalId)) {
      throw notFound("Medical record does not belong to the requested animal");
    }
    return toMedicalRecordView(record);
  }

  public AnimalDtos.MedicalRecordView updateMedicalRecord(
    UUID animalId,
    UUID recordId,
    AnimalDtos.UpsertMedicalRecordRequest body
  ) {
    requireAnimal(animalId);
    MedicalRecord record = requireMedicalRecord(recordId);
    if (!record.getAnimal().getId().equals(animalId)) {
      throw notFound("Medical record does not belong to the requested animal");
    }
    applyMedicalRecord(record, body);
    return toMedicalRecordView(record);
  }

  public void deleteMedicalRecord(UUID animalId, UUID recordId) {
    requireAnimal(animalId);
    MedicalRecord record = requireMedicalRecord(recordId);
    if (!record.getAnimal().getId().equals(animalId)) {
      throw notFound("Medical record does not belong to the requested animal");
    }
    medicalRecordRepository.delete(record);
  }

  private void applyShelter(Shelter shelter, AnimalDtos.UpsertShelterRequest body) {
    shelter.setName(normalizeName(body.name()));
    shelter.setCity(normalizeName(body.city()));
    shelter.setCountryCode(body.countryCode().trim().toUpperCase(Locale.ROOT));
    shelter.setContactEmail(body.contactEmail().trim().toLowerCase(Locale.ROOT));
    shelter.setContactPhone(normalizeNullable(body.contactPhone()));
    shelter.setAddress(normalizeNullable(body.address()));
  }

  private void applyBreed(Breed breed, AnimalDtos.UpsertBreedRequest body) {
    breed.setSpecies(requireSpecies(body.speciesId()));
    breed.setName(normalizeName(body.name()));
  }

  private void applyAnimal(Animal animal, AnimalDtos.UpsertAnimalRequest body) {
    Species species = requireSpecies(body.speciesId());
    Breed breed = requireCompatibleBreed(body.breedId(), species);
    animal.setName(normalizeName(body.name()));
    animal.setShelter(requireShelter(body.shelterId()));
    animal.setSpecies(species);
    animal.setBreed(breed);
    animal.setStatus(body.status());
    animal.setSex(body.sex());
    animal.setDescription(normalizeNullable(body.description()));
    animal.setBirthDate(body.birthDate());
    animal.setIntakeDate(body.intakeDate());
    animal.setAdoptionFee(body.adoptionFee());
    animal.setVaccinated(body.vaccinated());
    animal.setNeutered(body.neutered());
    animal.setTags(resolveTags(body.tagIds()));
  }

  private void applyMedicalRecord(
    MedicalRecord record,
    AnimalDtos.UpsertMedicalRecordRequest body
  ) {
    record.setTitle(normalizeName(body.title()));
    record.setExaminationDate(body.examinationDate());
    record.setTreatment(normalizeNullable(body.treatment()));
    record.setNotes(normalizeNullable(body.notes()));
    record.setWeightKg(body.weightKg());
    record.setFollowUpRequired(body.followUpRequired());
  }

  private Shelter requireShelter(UUID id) {
    return shelterRepository.findById(id).orElseThrow(() -> notFound("Shelter not found"));
  }

  private Species requireSpecies(UUID id) {
    return speciesRepository.findById(id).orElseThrow(() -> notFound("Species not found"));
  }

  private Breed requireBreed(UUID id) {
    return breedRepository.findById(id).orElseThrow(() -> notFound("Breed not found"));
  }

  private AnimalTag requireTag(UUID id) {
    return tagRepository.findById(id).orElseThrow(() -> notFound("Tag not found"));
  }

  private Animal requireAnimal(UUID id) {
    return animalRepository.findById(id).orElseThrow(() -> notFound("Animal not found"));
  }

  private MedicalRecord requireMedicalRecord(UUID id) {
    return medicalRecordRepository
      .findById(id)
      .orElseThrow(() -> notFound("Medical record not found"));
  }

  private @Nullable Breed requireCompatibleBreed(@Nullable UUID breedId, Species species) {
    if (breedId == null) {
      return null;
    }
    Breed breed = requireBreed(breedId);
    if (!breed.getSpecies().getId().equals(species.getId())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Breed does not match species");
    }
    return breed;
  }

  private Set<AnimalTag> resolveTags(@Nullable List<UUID> tagIds) {
    if (tagIds == null || tagIds.isEmpty()) {
      return new LinkedHashSet<>();
    }
    Set<UUID> uniqueIds = new LinkedHashSet<>(tagIds);
    List<AnimalTag> found = new ArrayList<>(tagRepository.findAllById(uniqueIds));
    if (found.size() != uniqueIds.size()) {
      throw notFound("Tag not found");
    }
    found.sort((left, right) -> left.getName().compareToIgnoreCase(right.getName()));
    return new LinkedHashSet<>(found);
  }

  private String normalizeName(String value) {
    return value.trim();
  }

  private @Nullable String normalizeNullable(@Nullable String value) {
    if (value == null) {
      return null;
    }
    String normalized = value.trim();
    return normalized.isEmpty() ? null : normalized;
  }

  private AnimalDtos.ShelterView toShelterView(Shelter shelter) {
    return new AnimalDtos.ShelterView(
      shelter.getId(),
      shelter.getName(),
      shelter.getCity(),
      shelter.getCountryCode(),
      shelter.getContactEmail(),
      shelter.getContactPhone(),
      shelter.getAddress()
    );
  }

  private AnimalDtos.SpeciesView toSpeciesView(Species species) {
    return new AnimalDtos.SpeciesView(species.getId(), species.getName());
  }

  private AnimalDtos.BreedView toBreedView(Breed breed) {
    return new AnimalDtos.BreedView(
      breed.getId(),
      breed.getSpecies().getId(),
      breed.getSpecies().getName(),
      breed.getName()
    );
  }

  private AnimalDtos.TagView toTagView(AnimalTag tag) {
    return new AnimalDtos.TagView(tag.getId(), tag.getName());
  }

  private List<AnimalDtos.TagView> toTagViews(Animal animal) {
    return animal
      .getTags()
      .stream()
      .sorted((left, right) -> left.getName().compareToIgnoreCase(right.getName()))
      .map(this::toTagView)
      .toList();
  }

  private AnimalDtos.AnimalSummary toAnimalSummary(Animal animal) {
    Breed breed = animal.getBreed();
    return new AnimalDtos.AnimalSummary(
      animal.getId(),
      animal.getName(),
      animal.getStatus(),
      animal.getSex(),
      animal.getShelter().getId(),
      animal.getShelter().getName(),
      animal.getSpecies().getId(),
      animal.getSpecies().getName(),
      breed == null ? null : breed.getId(),
      breed == null ? null : breed.getName(),
      toTagViews(animal)
    );
  }

  private AnimalDtos.AnimalView toAnimalView(Animal animal) {
    Breed breed = animal.getBreed();
    return new AnimalDtos.AnimalView(
      animal.getId(),
      animal.getName(),
      animal.getStatus(),
      animal.getSex(),
      animal.getShelter().getId(),
      animal.getShelter().getName(),
      animal.getSpecies().getId(),
      animal.getSpecies().getName(),
      breed == null ? null : breed.getId(),
      breed == null ? null : breed.getName(),
      animal.getDescription(),
      animal.getBirthDate(),
      animal.getIntakeDate(),
      animal.getAdoptionFee(),
      animal.isVaccinated(),
      animal.isNeutered(),
      toTagViews(animal)
    );
  }

  private AnimalDtos.MedicalRecordView toMedicalRecordView(MedicalRecord record) {
    return new AnimalDtos.MedicalRecordView(
      record.getId(),
      record.getAnimal().getId(),
      record.getTitle(),
      record.getExaminationDate(),
      record.getTreatment(),
      record.getNotes(),
      record.getWeightKg(),
      record.isFollowUpRequired()
    );
  }

  private ResponseStatusException notFound(String message) {
    return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
  }

  private ResponseStatusException conflict(String message) {
    return new ResponseStatusException(HttpStatus.CONFLICT, message);
  }
}
