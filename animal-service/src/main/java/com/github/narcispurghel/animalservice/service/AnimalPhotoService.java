package com.github.narcispurghel.animalservice.service;

import com.github.narcispurghel.animalservice.config.StorageProperties;
import com.github.narcispurghel.animalservice.dto.AnimalDtos;
import com.github.narcispurghel.animalservice.entity.Animal;
import com.github.narcispurghel.animalservice.entity.AnimalPhoto;
import com.github.narcispurghel.animalservice.repository.AnimalJpaRepository;
import com.github.narcispurghel.animalservice.repository.AnimalPhotoJpaRepository;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
@Transactional
public class AnimalPhotoService {

  private static final long MAX_SIZE_BYTES = 10L * 1024 * 1024;

  private final AnimalPhotoJpaRepository photoRepository;
  private final AnimalJpaRepository animalRepository;
  private final S3Client s3;
  private final StorageProperties storage;

  public AnimalPhotoService(
    AnimalPhotoJpaRepository photoRepository,
    AnimalJpaRepository animalRepository,
    S3Client s3,
    StorageProperties storage
  ) {
    this.photoRepository = photoRepository;
    this.animalRepository = animalRepository;
    this.s3 = s3;
    this.storage = storage;
  }

  @Transactional(readOnly = true)
  public List<AnimalDtos.AnimalPhotoView> list(UUID animalId) {
    requireAnimal(animalId);
    return photoRepository
      .findByAnimalIdOrderBySortOrderAscCreatedAtAsc(animalId)
      .stream()
      .map(AnimalPhotoService::toView)
      .toList();
  }

  public AnimalDtos.AnimalPhotoView upload(UUID animalId, MultipartFile file) {
    Animal animal = requireAnimal(animalId);
    if (file.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
    }
    if (file.getSize() > MAX_SIZE_BYTES) {
      throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "File exceeds 10MB");
    }
    String contentType = file.getContentType();
    if (contentType == null || !contentType.startsWith("image/")) {
      throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Only image uploads are allowed");
    }

    UUID photoId = UUID.randomUUID();
    String objectKey = "animals/" + animalId + "/" + photoId;
    int nextSortOrder =
      photoRepository.findByAnimalIdOrderBySortOrderAscCreatedAtAsc(animalId).stream()
        .mapToInt(AnimalPhoto::getSortOrder)
        .max()
        .orElse(-1) + 1;

    try {
      s3.putObject(
        PutObjectRequest.builder()
          .bucket(storage.bucket())
          .key(objectKey)
          .contentType(contentType)
          .contentLength(file.getSize())
          .build(),
        RequestBody.fromInputStream(file.getInputStream(), file.getSize())
      );
    } catch (IOException ex) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read upload", ex);
    } catch (S3Exception ex) {
      throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Object storage rejected upload", ex);
    }

    AnimalPhoto photo = new AnimalPhoto();
    photo.setAnimal(animal);
    photo.setObjectKey(objectKey);
    photo.setContentType(contentType);
    photo.setSortOrder(nextSortOrder);
    AnimalPhoto saved = photoRepository.save(photo);
    return toView(saved);
  }

  public void delete(UUID animalId, UUID photoId) {
    AnimalPhoto photo = requirePhoto(animalId, photoId);
    try {
      s3.deleteObject(
        DeleteObjectRequest.builder().bucket(storage.bucket()).key(photo.getObjectKey()).build()
      );
    } catch (S3Exception ignored) {
      // proceed with row deletion even if S3 delete fails; orphaned object can be cleaned later
    }
    photoRepository.delete(photo);
  }

  @Transactional(readOnly = true)
  public PhotoBytes download(UUID animalId, UUID photoId) {
    AnimalPhoto photo = requirePhoto(animalId, photoId);
    try {
      ResponseBytes<GetObjectResponse> obj = s3.getObjectAsBytes(
        GetObjectRequest.builder().bucket(storage.bucket()).key(photo.getObjectKey()).build()
      );
      return new PhotoBytes(obj.asByteArray(), photo.getContentType());
    } catch (NoSuchKeyException ex) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Photo bytes missing in storage", ex);
    } catch (S3Exception ex) {
      throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Object storage error", ex);
    }
  }

  private Animal requireAnimal(UUID id) {
    return animalRepository
      .findById(id)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Animal not found"));
  }

  private AnimalPhoto requirePhoto(UUID animalId, UUID photoId) {
    AnimalPhoto photo = photoRepository
      .findById(photoId)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Photo not found"));
    if (!photo.getAnimal().getId().equals(animalId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Photo not found");
    }
    return photo;
  }

  private static AnimalDtos.AnimalPhotoView toView(AnimalPhoto p) {
    return new AnimalDtos.AnimalPhotoView(
      p.getId(),
      p.getAnimal().getId(),
      p.getContentType(),
      p.getSortOrder(),
      p.getCreatedAt()
    );
  }

  public record PhotoBytes(byte[] bytes, String contentType) {}
}
