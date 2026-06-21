package com.github.narcispurghel.animalservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.narcispurghel.animalservice.config.StorageProperties;
import com.github.narcispurghel.animalservice.dto.AnimalDtos;
import com.github.narcispurghel.animalservice.entity.Animal;
import com.github.narcispurghel.animalservice.entity.AnimalPhoto;
import com.github.narcispurghel.animalservice.repository.AnimalJpaRepository;
import com.github.narcispurghel.animalservice.repository.AnimalPhotoJpaRepository;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.services.s3.S3Client;

class AnimalPhotoServiceTest {

  private final AnimalPhotoJpaRepository photoRepository = mock(AnimalPhotoJpaRepository.class);
  private final AnimalJpaRepository animalRepository = mock(AnimalJpaRepository.class);
  private final S3Client s3 = mock(S3Client.class);
  private final StorageProperties storage =
    new StorageProperties("http://localhost", "us-east-1", "key", "secret", "bucket");

  private final AnimalPhotoService service =
    new AnimalPhotoService(photoRepository, animalRepository, s3, storage);

  @Test
  void list_animalNotFound_throws404() {
    UUID animalId = UUID.randomUUID();
    when(animalRepository.findById(animalId)).thenReturn(Optional.empty());

    ResponseStatusException ex = catchThrowableOfType(
      () -> service.list(animalId),
      ResponseStatusException.class
    );

    assertThat(ex.getStatusCode().value()).isEqualTo(404);
  }

  @Test
  void upload_emptyFile_throws400() {
    UUID animalId = UUID.randomUUID();
    when(animalRepository.findById(animalId)).thenReturn(Optional.of(animal(animalId)));
    MultipartFile file = mock(MultipartFile.class);
    when(file.isEmpty()).thenReturn(true);

    ResponseStatusException ex = catchThrowableOfType(
      () -> service.upload(animalId, file),
      ResponseStatusException.class
    );

    assertThat(ex.getStatusCode().value()).isEqualTo(400);
  }

  @Test
  void upload_tooLarge_throws413() {
    UUID animalId = UUID.randomUUID();
    when(animalRepository.findById(animalId)).thenReturn(Optional.of(animal(animalId)));
    MultipartFile file = mock(MultipartFile.class);
    when(file.isEmpty()).thenReturn(false);
    when(file.getSize()).thenReturn(11L * 1024 * 1024);

    ResponseStatusException ex = catchThrowableOfType(
      () -> service.upload(animalId, file),
      ResponseStatusException.class
    );

    assertThat(ex.getStatusCode().value()).isEqualTo(413);
  }

  @Test
  void upload_nonImage_throws415() {
    UUID animalId = UUID.randomUUID();
    when(animalRepository.findById(animalId)).thenReturn(Optional.of(animal(animalId)));
    MultipartFile file = mock(MultipartFile.class);
    when(file.isEmpty()).thenReturn(false);
    when(file.getSize()).thenReturn(1024L);
    when(file.getContentType()).thenReturn("application/pdf");

    ResponseStatusException ex = catchThrowableOfType(
      () -> service.upload(animalId, file),
      ResponseStatusException.class
    );

    assertThat(ex.getStatusCode().value()).isEqualTo(415);
  }

  @Test
  void upload_image_savesAndReturnsView() throws Exception {
    UUID animalId = UUID.randomUUID();
    when(animalRepository.findById(animalId)).thenReturn(Optional.of(animal(animalId)));
    MultipartFile file = mock(MultipartFile.class);
    when(file.isEmpty()).thenReturn(false);
    when(file.getSize()).thenReturn(512L);
    when(file.getContentType()).thenReturn("image/png");
    when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[512]));
    when(photoRepository.findByAnimalIdOrderBySortOrderAscCreatedAtAsc(animalId))
      .thenReturn(List.of());
    when(photoRepository.save(any(AnimalPhoto.class))).thenAnswer(inv -> inv.getArgument(0));

    AnimalDtos.AnimalPhotoView view = service.upload(animalId, file);

    assertThat(view.animalId()).isEqualTo(animalId);
    assertThat(view.contentType()).isEqualTo("image/png");
    verify(photoRepository).save(any(AnimalPhoto.class));
  }

  @Test
  void delete_removesRow() {
    UUID animalId = UUID.randomUUID();
    UUID photoId = UUID.randomUUID();
    AnimalPhoto photo = photo(photoId, animalId);
    when(photoRepository.findById(photoId)).thenReturn(Optional.of(photo));

    service.delete(animalId, photoId);

    verify(photoRepository).delete(photo);
  }

  @Test
  void deleteAllForAnimal_removesEveryPhoto() {
    UUID animalId = UUID.randomUUID();
    List<AnimalPhoto> photos = List.of(
      photo(UUID.randomUUID(), animalId),
      photo(UUID.randomUUID(), animalId)
    );
    when(photoRepository.findByAnimalIdOrderBySortOrderAscCreatedAtAsc(animalId))
      .thenReturn(photos);

    service.deleteAllForAnimal(animalId);

    verify(photoRepository).deleteAll(photos);
  }

  private static Animal animal(UUID id) {
    Animal animal = new Animal();
    ReflectionTestUtils.setField(animal, "id", id);
    return animal;
  }

  private static AnimalPhoto photo(UUID id, UUID animalId) {
    AnimalPhoto photo = new AnimalPhoto();
    photo.setAnimal(animal(animalId));
    photo.setObjectKey("animals/" + animalId + "/" + id);
    photo.setContentType("image/png");
    photo.setSortOrder(0);
    ReflectionTestUtils.setField(photo, "id", id);
    return photo;
  }
}
