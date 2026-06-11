package com.github.narcispurghel.animalservice.controller;

import com.github.narcispurghel.animalservice.dto.AnimalDtos;
import com.github.narcispurghel.animalservice.service.AnimalPhotoService;
import com.github.narcispurghel.animalservice.service.AnimalPhotoService.PhotoBytes;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/animals/{animalId}/photos")
public class AnimalPhotoController {

  private final AnimalPhotoService photos;

  public AnimalPhotoController(AnimalPhotoService photos) {
    this.photos = photos;
  }

  @GetMapping
  public List<AnimalDtos.AnimalPhotoView> list(@PathVariable UUID animalId) {
    return photos.list(animalId);
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasRole('ADMIN')")
  public AnimalDtos.AnimalPhotoView upload(
    @PathVariable UUID animalId,
    @RequestPart("file") MultipartFile file
  ) {
    return photos.upload(animalId, file);
  }

  @DeleteMapping("/{photoId}")
  @PreAuthorize("hasRole('ADMIN')")
  public void delete(@PathVariable UUID animalId, @PathVariable UUID photoId) {
    photos.delete(animalId, photoId);
  }

  @GetMapping("/{photoId}")
  public ResponseEntity<byte[]> content(@PathVariable UUID animalId, @PathVariable UUID photoId) {
    PhotoBytes p = photos.download(animalId, photoId);
    return ResponseEntity.ok().contentType(MediaType.parseMediaType(p.contentType())).body(p.bytes());
  }
}
