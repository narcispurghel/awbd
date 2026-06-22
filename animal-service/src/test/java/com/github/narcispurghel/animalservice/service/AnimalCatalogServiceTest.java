package com.github.narcispurghel.animalservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.narcispurghel.animalservice.dto.AnimalDtos;
import com.github.narcispurghel.animalservice.entity.Animal;
import com.github.narcispurghel.animalservice.entity.AnimalStatus;
import com.github.narcispurghel.animalservice.entity.AnimalTag;
import com.github.narcispurghel.animalservice.entity.Breed;
import com.github.narcispurghel.animalservice.entity.MedicalRecord;
import com.github.narcispurghel.animalservice.entity.Sex;
import com.github.narcispurghel.animalservice.entity.Shelter;
import com.github.narcispurghel.animalservice.entity.Species;
import com.github.narcispurghel.animalservice.repository.AnimalJpaRepository;
import com.github.narcispurghel.animalservice.repository.AnimalPhotoJpaRepository;
import com.github.narcispurghel.animalservice.repository.AnimalTagJpaRepository;
import com.github.narcispurghel.animalservice.repository.BreedJpaRepository;
import com.github.narcispurghel.animalservice.repository.MedicalRecordJpaRepository;
import com.github.narcispurghel.animalservice.repository.ShelterJpaRepository;
import com.github.narcispurghel.animalservice.repository.SpeciesJpaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

class AnimalCatalogServiceTest {

  private final ShelterJpaRepository shelterRepository = mock(ShelterJpaRepository.class);
  private final SpeciesJpaRepository speciesRepository = mock(SpeciesJpaRepository.class);
  private final BreedJpaRepository breedRepository = mock(BreedJpaRepository.class);
  private final AnimalTagJpaRepository tagRepository = mock(AnimalTagJpaRepository.class);
  private final AnimalJpaRepository animalRepository = mock(AnimalJpaRepository.class);
  private final MedicalRecordJpaRepository medicalRecordRepository =
    mock(MedicalRecordJpaRepository.class);
  private final AnimalPhotoJpaRepository photoRepository = mock(AnimalPhotoJpaRepository.class);
  private final AnimalPhotoService animalPhotoService =
    new AnimalPhotoService(
      photoRepository,
      animalRepository,
      mock(software.amazon.awssdk.services.s3.S3Client.class),
      new com.github.narcispurghel.animalservice.config.StorageProperties(
        "http://localhost",
        "us-east-1",
        "key",
        "secret",
        "bucket"
      )
    );

  private final AnimalCatalogService service =
    new AnimalCatalogService(
      shelterRepository,
      speciesRepository,
      breedRepository,
      tagRepository,
      animalRepository,
      medicalRecordRepository,
      animalPhotoService
    );

  // ----- shelters -----

  @Test
  void createShelter_normalizesAndReturnsView() {
    when(shelterRepository.save(any(Shelter.class))).thenAnswer(inv -> inv.getArgument(0));

    AnimalDtos.ShelterView view = service.createShelter(
      new AnimalDtos.UpsertShelterRequest(
        "  Safe Paws  ",
        "Cluj-Napoca",
        "ro",
        "Contact@Safe.TEST",
        null,
        null
      )
    );

    assertThat(view.name()).isEqualTo("Safe Paws");
    assertThat(view.countryCode()).isEqualTo("RO");
    assertThat(view.contactEmail()).isEqualTo("contact@safe.test");
  }

  @Test
  void updateShelter_notFound_throws404() {
    UUID id = UUID.randomUUID();
    when(shelterRepository.findById(id)).thenReturn(Optional.empty());

    ResponseStatusException ex = catchThrowableOfType(
      () -> service.updateShelter(id, shelterRequest()),
      ResponseStatusException.class
    );

    assertThat(ex).isNotNull();
    assertThat(ex.getStatusCode().value()).isEqualTo(404);
  }

  @Test
  void deleteShelter_withAnimals_throws409() {
    UUID id = UUID.randomUUID();
    when(shelterRepository.findById(id)).thenReturn(Optional.of(shelter(id)));
    when(animalRepository.existsByShelterId(id)).thenReturn(true);

    ResponseStatusException ex = catchThrowableOfType(
      () -> service.deleteShelter(id),
      ResponseStatusException.class
    );

    assertThat(ex.getStatusCode().value()).isEqualTo(409);
    verify(shelterRepository, never()).delete(any());
  }

  @Test
  void deleteShelter_unused_deletes() {
    UUID id = UUID.randomUUID();
    Shelter shelter = shelter(id);
    when(shelterRepository.findById(id)).thenReturn(Optional.of(shelter));
    when(animalRepository.existsByShelterId(id)).thenReturn(false);

    service.deleteShelter(id);

    verify(shelterRepository).delete(shelter);
  }

  // ----- species -----

  @Test
  void deleteSpecies_withBreeds_throws409() {
    UUID id = UUID.randomUUID();
    when(speciesRepository.findById(id)).thenReturn(Optional.of(species(id, "Dog")));
    when(breedRepository.existsBySpeciesId(id)).thenReturn(true);

    ResponseStatusException ex = catchThrowableOfType(
      () -> service.deleteSpecies(id),
      ResponseStatusException.class
    );

    assertThat(ex.getStatusCode().value()).isEqualTo(409);
  }

  @Test
  void deleteSpecies_withAnimals_throws409() {
    UUID id = UUID.randomUUID();
    when(speciesRepository.findById(id)).thenReturn(Optional.of(species(id, "Dog")));
    when(breedRepository.existsBySpeciesId(id)).thenReturn(false);
    when(animalRepository.existsBySpeciesId(id)).thenReturn(true);

    ResponseStatusException ex = catchThrowableOfType(
      () -> service.deleteSpecies(id),
      ResponseStatusException.class
    );

    assertThat(ex.getStatusCode().value()).isEqualTo(409);
  }

  @Test
  void deleteSpecies_unused_deletes() {
    UUID id = UUID.randomUUID();
    Species species = species(id, "Dog");
    when(speciesRepository.findById(id)).thenReturn(Optional.of(species));
    when(breedRepository.existsBySpeciesId(id)).thenReturn(false);
    when(animalRepository.existsBySpeciesId(id)).thenReturn(false);

    service.deleteSpecies(id);

    verify(speciesRepository).delete(species);
  }

  // ----- breeds -----

  @Test
  void deleteBreed_withAnimals_throws409() {
    UUID id = UUID.randomUUID();
    when(breedRepository.findById(id)).thenReturn(Optional.of(breed(id, species(UUID.randomUUID(), "Dog"), "Lab")));
    when(animalRepository.existsByBreedId(id)).thenReturn(true);

    ResponseStatusException ex = catchThrowableOfType(
      () -> service.deleteBreed(id),
      ResponseStatusException.class
    );

    assertThat(ex.getStatusCode().value()).isEqualTo(409);
  }

  // ----- tags -----

  @Test
  void createTag_normalizesAndReturnsView() {
    when(tagRepository.save(any(AnimalTag.class))).thenAnswer(inv -> inv.getArgument(0));

    AnimalDtos.TagView view = service.createTag(new AnimalDtos.UpsertTagRequest("  Good with kids  "));

    assertThat(view.name()).isEqualTo("Good with kids");
  }

  @Test
  void deleteTag_assignedToAnimals_throws409() {
    UUID id = UUID.randomUUID();
    when(tagRepository.findById(id)).thenReturn(Optional.of(tag(id, "Senior")));
    when(animalRepository.existsByTagsId(id)).thenReturn(true);

    ResponseStatusException ex = catchThrowableOfType(
      () -> service.deleteTag(id),
      ResponseStatusException.class
    );

    assertThat(ex.getStatusCode().value()).isEqualTo(409);
    verify(tagRepository, never()).delete(any());
  }

  // ----- animals -----

  @Test
  void createAnimal_withCompatibleBreed_returnsView() {
    UUID shelterId = UUID.randomUUID();
    UUID speciesId = UUID.randomUUID();
    UUID breedId = UUID.randomUUID();
    Species species = species(speciesId, "Dog");
    AnimalTag tag = tag(UUID.randomUUID(), "Good with kids");
    when(shelterRepository.findById(shelterId)).thenReturn(Optional.of(shelter(shelterId)));
    when(speciesRepository.findById(speciesId)).thenReturn(Optional.of(species));
    when(breedRepository.findById(breedId)).thenReturn(Optional.of(breed(breedId, species, "Labrador")));
    when(tagRepository.findAllById(any())).thenReturn(List.of(tag));
    when(animalRepository.save(any(Animal.class))).thenAnswer(inv -> inv.getArgument(0));

    AnimalDtos.AnimalView view = service.createAnimal(
      new AnimalDtos.UpsertAnimalRequest(
        "Milo",
        shelterId,
        speciesId,
        breedId,
        AnimalStatus.AVAILABLE,
        Sex.MALE,
        "Friendly",
        LocalDate.of(2023, 1, 1),
        LocalDate.of(2025, 1, 1),
        new BigDecimal("120.00"),
        true,
        true,
        List.of(tag.getId())
      )
    );

    assertThat(view.name()).isEqualTo("Milo");
    assertThat(view.speciesName()).isEqualTo("Dog");
    assertThat(view.breedName()).isEqualTo("Labrador");
    assertThat(view.status()).isEqualTo(AnimalStatus.AVAILABLE);
    assertThat(view.tags()).extracting(AnimalDtos.TagView::name).containsExactly("Good with kids");
  }

  @Test
  void createAnimal_breedFromAnotherSpecies_throws400() {
    UUID speciesId = UUID.randomUUID();
    UUID otherSpeciesId = UUID.randomUUID();
    UUID breedId = UUID.randomUUID();
    when(speciesRepository.findById(speciesId)).thenReturn(Optional.of(species(speciesId, "Dog")));
    when(breedRepository.findById(breedId))
      .thenReturn(Optional.of(breed(breedId, species(otherSpeciesId, "Cat"), "Persian")));

    ResponseStatusException ex = catchThrowableOfType(
      () ->
        service.createAnimal(
          new AnimalDtos.UpsertAnimalRequest(
            "Milo",
            UUID.randomUUID(),
            speciesId,
            breedId,
            AnimalStatus.AVAILABLE,
            Sex.MALE,
            null,
            null,
            LocalDate.of(2025, 1, 1),
            null,
            false,
            false,
            List.of()
          )
        ),
      ResponseStatusException.class
    );

    assertThat(ex.getStatusCode().value()).isEqualTo(400);
  }

  @Test
  void animal_notFound_throws404() {
    UUID id = UUID.randomUUID();
    when(animalRepository.findById(id)).thenReturn(Optional.empty());

    ResponseStatusException ex = catchThrowableOfType(
      () -> service.animal(id),
      ResponseStatusException.class
    );

    assertThat(ex.getStatusCode().value()).isEqualTo(404);
  }

  @Test
  void deleteAnimal_removesPhotosRecordsAndAnimal() {
    UUID id = UUID.randomUUID();
    Animal animal = animal(id);
    when(animalRepository.findById(id)).thenReturn(Optional.of(animal));
    when(medicalRecordRepository.findByAnimalIdOrderByExaminationDateDesc(id))
      .thenReturn(List.of());

    service.deleteAnimal(id);

    verify(medicalRecordRepository).deleteAll(anyList());
    verify(animalRepository).delete(animal);
  }

  // ----- medical records -----

  @Test
  void addMedicalRecord_returnsView() {
    UUID animalId = UUID.randomUUID();
    when(animalRepository.findById(animalId)).thenReturn(Optional.of(animal(animalId)));
    when(medicalRecordRepository.save(any(MedicalRecord.class))).thenAnswer(inv -> inv.getArgument(0));

    AnimalDtos.MedicalRecordView view = service.addMedicalRecord(
      animalId,
      new AnimalDtos.UpsertMedicalRecordRequest(
        "Initial exam",
        LocalDate.of(2025, 1, 11),
        "Vaccines",
        "Healthy",
        new BigDecimal("18.40"),
        false
      )
    );

    assertThat(view.title()).isEqualTo("Initial exam");
    assertThat(view.animalId()).isEqualTo(animalId);
  }

  @Test
  void updateMedicalRecord_wrongAnimal_throws404() {
    UUID animalId = UUID.randomUUID();
    UUID recordId = UUID.randomUUID();
    MedicalRecord record = new MedicalRecord();
    record.setAnimal(animal(UUID.randomUUID()));
    ReflectionTestUtils.setField(record, "id", recordId);
    when(animalRepository.findById(animalId)).thenReturn(Optional.of(animal(animalId)));
    when(medicalRecordRepository.findById(recordId)).thenReturn(Optional.of(record));

    ResponseStatusException ex = catchThrowableOfType(
      () ->
        service.updateMedicalRecord(
          animalId,
          recordId,
          new AnimalDtos.UpsertMedicalRecordRequest(
            "Update",
            LocalDate.of(2025, 2, 1),
            null,
            null,
            null,
            false
          )
        ),
      ResponseStatusException.class
    );

    assertThat(ex.getStatusCode().value()).isEqualTo(404);
  }

  @Test
  void deleteMedicalRecord_ownedByAnimal_deletes() {
    UUID animalId = UUID.randomUUID();
    UUID recordId = UUID.randomUUID();
    Animal animal = animal(animalId);
    MedicalRecord record = new MedicalRecord();
    record.setAnimal(animal);
    ReflectionTestUtils.setField(record, "id", recordId);
    when(animalRepository.findById(animalId)).thenReturn(Optional.of(animal));
    when(medicalRecordRepository.findById(recordId)).thenReturn(Optional.of(record));

    service.deleteMedicalRecord(animalId, recordId);

    verify(medicalRecordRepository).delete(record);
  }

  // ----- helpers -----

  private static AnimalDtos.UpsertShelterRequest shelterRequest() {
    return new AnimalDtos.UpsertShelterRequest(
      "Safe Paws",
      "Cluj",
      "RO",
      "a@b.test",
      null,
      null
    );
  }

  private static Shelter shelter(UUID id) {
    Shelter shelter = new Shelter();
    shelter.setName("Safe Paws");
    shelter.setCity("Cluj");
    shelter.setCountryCode("RO");
    shelter.setContactEmail("a@b.test");
    ReflectionTestUtils.setField(shelter, "id", id);
    return shelter;
  }

  private static Species species(UUID id, String name) {
    Species species = new Species();
    species.setName(name);
    ReflectionTestUtils.setField(species, "id", id);
    return species;
  }

  private static Breed breed(UUID id, Species species, String name) {
    Breed breed = new Breed();
    breed.setSpecies(species);
    breed.setName(name);
    ReflectionTestUtils.setField(breed, "id", id);
    return breed;
  }

  private static AnimalTag tag(UUID id, String name) {
    AnimalTag tag = new AnimalTag();
    tag.setName(name);
    ReflectionTestUtils.setField(tag, "id", id);
    return tag;
  }

  private static Animal animal(UUID id) {
    Animal animal = new Animal();
    animal.setName("Milo");
    animal.setShelter(shelter(UUID.randomUUID()));
    animal.setSpecies(species(UUID.randomUUID(), "Dog"));
    animal.setStatus(AnimalStatus.AVAILABLE);
    animal.setSex(Sex.MALE);
    animal.setIntakeDate(LocalDate.of(2025, 1, 1));
    ReflectionTestUtils.setField(animal, "id", id);
    return animal;
  }
}
