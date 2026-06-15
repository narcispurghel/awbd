package com.github.narcispurghel.animalservice.config;

import com.github.narcispurghel.animalservice.entity.Animal;
import com.github.narcispurghel.animalservice.entity.AnimalPhoto;
import com.github.narcispurghel.animalservice.entity.AnimalStatus;
import com.github.narcispurghel.animalservice.entity.Breed;
import com.github.narcispurghel.animalservice.entity.MedicalRecord;
import com.github.narcispurghel.animalservice.entity.Sex;
import com.github.narcispurghel.animalservice.entity.Shelter;
import com.github.narcispurghel.animalservice.entity.Species;
import com.github.narcispurghel.animalservice.repository.AnimalJpaRepository;
import com.github.narcispurghel.animalservice.repository.AnimalPhotoJpaRepository;
import com.github.narcispurghel.animalservice.repository.BreedJpaRepository;
import com.github.narcispurghel.animalservice.repository.MedicalRecordJpaRepository;
import com.github.narcispurghel.animalservice.repository.ShelterJpaRepository;
import com.github.narcispurghel.animalservice.repository.SpeciesJpaRepository;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import javax.imageio.ImageIO;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
@Profile("dev")
@Order(100)
public class DevSeedRunner implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(DevSeedRunner.class);

  private final ShelterJpaRepository shelters;
  private final SpeciesJpaRepository species;
  private final BreedJpaRepository breeds;
  private final AnimalJpaRepository animals;
  private final MedicalRecordJpaRepository medicalRecords;
  private final AnimalPhotoJpaRepository photos;
  private final S3Client s3;
  private final StorageProperties storage;

  public DevSeedRunner(
    ShelterJpaRepository shelters,
    SpeciesJpaRepository species,
    BreedJpaRepository breeds,
    AnimalJpaRepository animals,
    MedicalRecordJpaRepository medicalRecords,
    AnimalPhotoJpaRepository photos,
    S3Client s3,
    StorageProperties storage
  ) {
    this.shelters = shelters;
    this.species = species;
    this.breeds = breeds;
    this.animals = animals;
    this.medicalRecords = medicalRecords;
    this.photos = photos;
    this.s3 = s3;
    this.storage = storage;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    Map<String, Shelter> shelterByName = indexBy(shelters.findAll(), Shelter::getName);
    Map<String, Species> speciesByName = indexBy(species.findAll(), Species::getName);
    Map<String, Breed> breedByKey = new HashMap<>();
    for (Breed b : breeds.findAll()) {
      breedByKey.put(b.getSpecies().getName() + ":" + b.getName(), b);
    }

    int addedShelters = 0;
    for (ShelterSeed s : SHELTER_SEEDS) {
      if (shelterByName.containsKey(s.name)) continue;
      Shelter sh = new Shelter();
      sh.setName(s.name);
      sh.setCity(s.city);
      sh.setCountryCode(s.country);
      sh.setContactEmail(s.email);
      sh.setContactPhone(s.phone);
      sh.setAddress(s.address);
      shelterByName.put(s.name, shelters.save(sh));
      addedShelters++;
    }

    int addedSpecies = 0;
    for (String name : SPECIES_NAMES) {
      if (speciesByName.containsKey(name)) continue;
      Species sp = new Species();
      sp.setName(name);
      speciesByName.put(name, species.save(sp));
      addedSpecies++;
    }

    int addedBreeds = 0;
    for (BreedSeed b : BREED_SEEDS) {
      String key = b.speciesName + ":" + b.name;
      if (breedByKey.containsKey(key)) continue;
      Breed br = new Breed();
      br.setName(b.name);
      br.setSpecies(require(speciesByName, b.speciesName, "species"));
      breedByKey.put(key, breeds.save(br));
      addedBreeds++;
    }

    Map<String, Animal> animalByShelterAndName = new HashMap<>();
    for (Animal a : animals.findAll()) {
      animalByShelterAndName.put(a.getShelter().getName() + ":" + a.getName(), a);
    }

    int addedAnimals = 0;
    int addedPhotos = 0;
    int photoIndex = 0;
    for (AnimalSeed a : ANIMAL_SEEDS) {
      String key = a.shelterName + ":" + a.name;
      Animal saved = animalByShelterAndName.get(key);
      if (saved == null) {
        Animal animal = new Animal();
        animal.setName(a.name);
        animal.setShelter(require(shelterByName, a.shelterName, "shelter"));
        animal.setSpecies(require(speciesByName, a.speciesName, "species"));
        animal.setBreed(require(breedByKey, a.speciesName + ":" + a.breedName, "breed"));
        animal.setSex(a.sex);
        animal.setStatus(a.status);
        animal.setDescription(a.description);
        animal.setBirthDate(a.birthDate);
        animal.setIntakeDate(a.intakeDate);
        animal.setAdoptionFee(a.adoptionFee);
        animal.setVaccinated(a.vaccinated);
        animal.setNeutered(a.neutered);
        saved = animals.save(animal);
        animalByShelterAndName.put(key, saved);

        for (MedicalSeed m : a.medical) {
          MedicalRecord rec = new MedicalRecord();
          rec.setAnimal(saved);
          rec.setTitle(m.title);
          rec.setExaminationDate(m.date);
          rec.setTreatment(m.treatment);
          rec.setNotes(m.notes);
          rec.setWeightKg(m.weightKg);
          rec.setFollowUpRequired(m.followUp);
          medicalRecords.save(rec);
        }
        addedAnimals++;
      }

      // Backfill a photo if this seed animal has none yet (covers the "swap in real images later" flow).
      if (photos.findByAnimalIdOrderBySortOrderAscCreatedAtAsc(saved.getId()).isEmpty()) {
        if (uploadPhoto(saved, a.photoFile, photoIndex)) {
          addedPhotos++;
        }
      }
      photoIndex++;
    }

    if (addedShelters + addedSpecies + addedBreeds + addedAnimals + addedPhotos == 0) {
      log.info("DevSeedRunner: catalog already complete, nothing to add");
    } else {
      log.info(
        "DevSeedRunner: added {} shelters, {} species, {} breeds, {} animals, {} photos",
        addedShelters, addedSpecies, addedBreeds, addedAnimals, addedPhotos
      );
    }
  }

  private static <E> Map<String, E> indexBy(List<E> items, Function<E, String> keyFn) {
    Map<String, E> map = new HashMap<>();
    for (E item : items) {
      map.put(keyFn.apply(item), item);
    }
    return map;
  }

  private boolean uploadPhoto(Animal animal, String photoFile, int paletteIndex) {
    LoadedImage image = loadSeedImage(photoFile);
    if (image == null) {
      try {
        image = new LoadedImage(generateImage(animal.getName(), paletteIndex), "image/png");
      } catch (IOException ex) {
        log.warn(
          "DevSeedRunner: failed to render placeholder image for '{}': {}",
          animal.getName(), ex.getMessage()
        );
        return false;
      }
    }

    UUID photoId = UUID.randomUUID();
    String objectKey = "animals/" + animal.getId() + "/" + photoId;
    try {
      s3.putObject(
        PutObjectRequest.builder()
          .bucket(storage.bucket())
          .key(objectKey)
          .contentType(image.contentType)
          .contentLength((long) image.bytes.length)
          .build(),
        RequestBody.fromBytes(image.bytes)
      );
    } catch (RuntimeException ex) {
      log.warn(
        "DevSeedRunner: failed to upload seed image for '{}' to bucket '{}': {}",
        animal.getName(), storage.bucket(), ex.getMessage()
      );
      return false;
    }

    AnimalPhoto photo = new AnimalPhoto();
    photo.setAnimal(animal);
    photo.setObjectKey(objectKey);
    photo.setContentType(image.contentType);
    photo.setSortOrder(0);
    photos.save(photo);
    return true;
  }

  private @Nullable LoadedImage loadSeedImage(String filename) {
    String resource = "/seed/photos/" + filename;
    try (InputStream in = DevSeedRunner.class.getResourceAsStream(resource)) {
      if (in == null) return null;
      byte[] bytes = in.readAllBytes();
      if (bytes.length == 0) return null;
      return new LoadedImage(bytes, contentTypeFor(filename));
    } catch (IOException ex) {
      log.warn("DevSeedRunner: failed to read seed image '{}': {}", resource, ex.getMessage());
      return null;
    }
  }

  private static String contentTypeFor(String filename) {
    String lower = filename.toLowerCase(Locale.ROOT);
    if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
    if (lower.endsWith(".webp")) return "image/webp";
    if (lower.endsWith(".gif")) return "image/gif";
    return "image/png";
  }

  private static byte[] generateImage(String label, int paletteIndex) throws IOException {
    int size = 480;
    BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
    Graphics2D g = img.createGraphics();
    try {
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

      Color[] palette = PALETTE[Math.floorMod(paletteIndex, PALETTE.length)];
      g.setPaint(new GradientPaint(0, 0, palette[0], size, size, palette[1]));
      g.fillRect(0, 0, size, size);

      g.setColor(new Color(255, 255, 255, 60));
      int circleSize = (int) (size * 0.55);
      g.fillOval((size - circleSize) / 2, (size - circleSize) / 2 - 30, circleSize, circleSize);

      g.setColor(Color.WHITE);
      g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 56));
      int textWidth = g.getFontMetrics().stringWidth(label);
      g.drawString(label, (size - textWidth) / 2, size - 80);

      g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 22));
      String sub = "PAWS · Adopt me";
      int subWidth = g.getFontMetrics().stringWidth(sub);
      g.drawString(sub, (size - subWidth) / 2, size - 40);
    } finally {
      g.dispose();
    }
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ImageIO.write(img, "png", out);
    return out.toByteArray();
  }

  private static final Color[][] PALETTE = {
    {new Color(0xFF6B6B), new Color(0xFFA94D)},
    {new Color(0x4DABF7), new Color(0x9775FA)},
    {new Color(0x51CF66), new Color(0x20C997)},
    {new Color(0xF783AC), new Color(0xDA77F2)},
    {new Color(0xFFD43B), new Color(0xFF922B)},
    {new Color(0x748FFC), new Color(0x5C7CFA)},
    {new Color(0x63E6BE), new Color(0x38D9A9)},
    {new Color(0xFFB7C5), new Color(0xFF6B9D)},
  };

  private static <V> V require(Map<String, V> map, String key, String label) {
    return Objects.requireNonNull(map.get(key), () -> "Missing " + label + " seed: " + key);
  }

  private record LoadedImage(byte[] bytes, String contentType) {}

  private record ShelterSeed(
    String name, String city, String country, String email, String phone, String address
  ) {}

  private record BreedSeed(String speciesName, String name) {}

  private record MedicalSeed(
    String title,
    LocalDate date,
    @Nullable String treatment,
    @Nullable String notes,
    @Nullable BigDecimal weightKg,
    boolean followUp
  ) {}

  private record AnimalSeed(
    String name,
    String shelterName,
    String speciesName,
    String breedName,
    Sex sex,
    AnimalStatus status,
    String description,
    LocalDate birthDate,
    LocalDate intakeDate,
    BigDecimal adoptionFee,
    boolean vaccinated,
    boolean neutered,
    String photoFile,
    List<MedicalSeed> medical
  ) {}

  private static final ShelterSeed[] SHELTER_SEEDS = {
    new ShelterSeed("PAWS Bucharest",        "Bucharest",   "RO", "contact@paws-bucharest.ro", "+40213000111", "Str. Veseliei 14, Sector 3"),
    new ShelterSeed("Happy Tails Cluj",      "Cluj-Napoca", "RO", "hello@happytails.ro",       "+40264555222", "Str. Memorandumului 8"),
    new ShelterSeed("Hope Shelter Iași",     "Iași",        "RO", "adopt@hopeshelter.ro",      "+40232777333", "Bd. Carol I 35"),
    new ShelterSeed("Chișinău Animal Rescue","Chișinău",    "MD", "team@chisinaurescue.md",    "+37322444555", "Str. Mihai Eminescu 12"),
    new ShelterSeed("Brașov Pet Haven",      "Brașov",      "RO", "info@pethaven.ro",          "+40268999444", "Str. Iuliu Maniu 22"),
  };

  private static final String[] SPECIES_NAMES = {"Dog", "Cat", "Rabbit", "Bird"};

  private static final BreedSeed[] BREED_SEEDS = {
    new BreedSeed("Dog",    "Labrador Retriever"),
    new BreedSeed("Dog",    "German Shepherd"),
    new BreedSeed("Dog",    "Mixed Breed"),
    new BreedSeed("Dog",    "Husky"),
    new BreedSeed("Dog",    "Beagle"),
    new BreedSeed("Cat",    "Mixed Breed"),
    new BreedSeed("Cat",    "Persian"),
    new BreedSeed("Cat",    "Maine Coon"),
    new BreedSeed("Rabbit", "Holland Lop"),
    new BreedSeed("Rabbit", "Mixed Breed"),
    new BreedSeed("Bird",   "Budgerigar"),
  };

  private static final AnimalSeed[] ANIMAL_SEEDS = animalSeeds();

  private static AnimalSeed[] animalSeeds() {
    LocalDate today = LocalDate.now(ZoneId.systemDefault());
    return new AnimalSeed[] {
      new AnimalSeed(
        "Bruno", "PAWS Bucharest", "Dog", "Labrador Retriever",
        Sex.MALE, AnimalStatus.AVAILABLE,
        "Gentle giant who loves long walks and belly rubs. Great with kids.",
        today.minusYears(3), today.minusMonths(5), new BigDecimal("250.00"),
        true, true,
        "bruno-labrador.jpg",
        List.of(
          new MedicalSeed("Initial intake exam", today.minusMonths(5), "Vaccines, deworming", "Healthy overall", new BigDecimal("28.40"), false),
          new MedicalSeed("Annual checkup", today.minusMonths(1), null, "Weight stable, teeth in good shape", new BigDecimal("29.10"), false)
        )
      ),
      new AnimalSeed(
        "Luna", "PAWS Bucharest", "Cat", "Mixed Breed",
        Sex.FEMALE, AnimalStatus.AVAILABLE,
        "Curious tabby rescued from the streets. Loves sunny windowsills.",
        today.minusYears(2), today.minusMonths(3), new BigDecimal("80.00"),
        true, true,
        "luna-tabby.jpg",
        List.of(
          new MedicalSeed("Spay surgery", today.minusMonths(3), "Ovariohysterectomy", "Recovered well", new BigDecimal("3.80"), false)
        )
      ),
      new AnimalSeed(
        "Max", "Happy Tails Cluj", "Dog", "German Shepherd",
        Sex.MALE, AnimalStatus.AVAILABLE,
        "Calm and obedient ex-service dog looking for a quiet home.",
        today.minusYears(6), today.minusMonths(2), new BigDecimal("180.00"),
        true, true,
        "max-german-shepherd.jpg",
        List.of(
          new MedicalSeed("Hip x-ray", today.minusMonths(2), "Anti-inflammatory", "Mild hip dysplasia, manageable", new BigDecimal("32.50"), true)
        )
      ),
      new AnimalSeed(
        "Misu", "Happy Tails Cluj", "Cat", "Persian",
        Sex.MALE, AnimalStatus.AVAILABLE,
        "Fluffy lap cat. Needs daily brushing and quiet adopters.",
        today.minusYears(4), today.minusWeeks(6), new BigDecimal("120.00"),
        true, false,
        "misu-persian.jpg",
        List.of(
          new MedicalSeed("Dental cleaning", today.minusWeeks(5), "Scaling", "Two molars extracted", new BigDecimal("4.20"), false)
        )
      ),
      new AnimalSeed(
        "Rocky", "Happy Tails Cluj", "Dog", "Husky",
        Sex.MALE, AnimalStatus.RESERVED,
        "High-energy boy. Needs a yard and at least one hour of exercise per day.",
        today.minusYears(2), today.minusMonths(1), new BigDecimal("200.00"),
        true, false,
        "rocky-husky.jpg",
        List.of(
          new MedicalSeed("Eye exam", today.minusMonths(1), null, "No anomalies", null, false)
        )
      ),
      new AnimalSeed(
        "Bella", "Hope Shelter Iași", "Dog", "Mixed Breed",
        Sex.FEMALE, AnimalStatus.AVAILABLE,
        "Friendly mid-size mutt rescued with her puppies (now adopted).",
        today.minusYears(5), today.minusMonths(8), new BigDecimal("150.00"),
        true, true,
        "bella-mixed-dog.jpg",
        List.of(
          new MedicalSeed("Vaccination booster", today.minusMonths(2), "DHPP + Rabies", null, new BigDecimal("18.60"), false),
          new MedicalSeed("Spay surgery", today.minusMonths(7), "Ovariohysterectomy", "Smooth recovery", new BigDecimal("18.20"), false)
        )
      ),
      new AnimalSeed(
        "Cleo", "Hope Shelter Iași", "Cat", "Maine Coon",
        Sex.FEMALE, AnimalStatus.AVAILABLE,
        "Majestic long-haired cat, gentle but reserved with strangers.",
        today.minusYears(3), today.minusMonths(4), new BigDecimal("160.00"),
        true, true,
        "cleo-maine-coon.jpg",
        List.of(
          new MedicalSeed("Annual checkup", today.minusMonths(1), null, "Healthy, coat in great condition", new BigDecimal("5.40"), false)
        )
      ),
      new AnimalSeed(
        "Toby", "Hope Shelter Iași", "Dog", "Beagle",
        Sex.MALE, AnimalStatus.MEDICAL_HOLD,
        "Sweet beagle currently recovering from ear infection treatment.",
        today.minusYears(4), today.minusMonths(1), new BigDecimal("130.00"),
        true, true,
        "toby-beagle.jpg",
        List.of(
          new MedicalSeed("Ear infection treatment", today.minusWeeks(3), "Otic drops, antibiotic", "Re-check in 2 weeks", new BigDecimal("14.10"), true)
        )
      ),
      new AnimalSeed(
        "Hopa", "Hope Shelter Iași", "Rabbit", "Holland Lop",
        Sex.FEMALE, AnimalStatus.AVAILABLE,
        "Tiny lop-eared rabbit. Loves hay tunnels and gentle company.",
        today.minusYears(1), today.minusMonths(2), new BigDecimal("60.00"),
        false, false,
        "hopa-holland-lop.jpg",
        List.of(
          new MedicalSeed("Initial check", today.minusMonths(2), null, "Healthy, slightly underweight", new BigDecimal("1.10"), false)
        )
      ),
      new AnimalSeed(
        "Charlie", "Chișinău Animal Rescue", "Dog", "Mixed Breed",
        Sex.MALE, AnimalStatus.AVAILABLE,
        "Small lively pup, would do well in an apartment with another dog.",
        today.minusYears(1), today.minusMonths(3), new BigDecimal("100.00"),
        true, false,
        "charlie-mixed-dog.jpg",
        List.of(
          new MedicalSeed("Puppy vaccination round", today.minusMonths(3), "DHPP + Lepto", null, new BigDecimal("6.20"), false)
        )
      ),
      new AnimalSeed(
        "Mimi", "Chișinău Animal Rescue", "Cat", "Mixed Breed",
        Sex.FEMALE, AnimalStatus.ADOPTED,
        "Sweet calico cat already adopted — kept here for archive demo.",
        today.minusYears(5), today.minusYears(1), new BigDecimal("70.00"),
        true, true,
        "mimi-calico.jpg",
        List.of()
      ),
      new AnimalSeed(
        "Bobita", "Chișinău Animal Rescue", "Rabbit", "Mixed Breed",
        Sex.MALE, AnimalStatus.AVAILABLE,
        "Calm grey rabbit. Used to children and small spaces.",
        today.minusYears(2), today.minusMonths(5), new BigDecimal("50.00"),
        false, false,
        "bobita-mixed-rabbit.jpg",
        List.of(
          new MedicalSeed("Routine check", today.minusMonths(1), null, "All good", new BigDecimal("1.60"), false)
        )
      ),
      new AnimalSeed(
        "Kiwi", "Brașov Pet Haven", "Bird", "Budgerigar",
        Sex.UNKNOWN, AnimalStatus.AVAILABLE,
        "Bright green budgie, chatty in the morning. Adoption includes cage.",
        today.minusYears(1), today.minusWeeks(8), new BigDecimal("40.00"),
        false, false,
        "kiwi-budgerigar.jpg",
        List.of()
      ),
      new AnimalSeed(
        "Rex", "Brașov Pet Haven", "Dog", "Labrador Retriever",
        Sex.MALE, AnimalStatus.AVAILABLE,
        "Mellow senior lab. Perfect for a quiet family without small kids.",
        today.minusYears(9), today.minusMonths(2), new BigDecimal("120.00"),
        true, true,
        "rex-senior-labrador.jpg",
        List.of(
          new MedicalSeed("Geriatric blood panel", today.minusMonths(2), null, "Mild kidney values, monitor", new BigDecimal("31.80"), true),
          new MedicalSeed("Arthritis management", today.minusWeeks(3), "Joint supplement", "Walking comfortably", new BigDecimal("31.20"), false)
        )
      ),
      new AnimalSeed(
        "Pisi", "Brașov Pet Haven", "Cat", "Mixed Breed",
        Sex.FEMALE, AnimalStatus.AVAILABLE,
        "Shy black cat looking for a patient home with no other pets.",
        today.minusYears(2), today.minusMonths(1), new BigDecimal("60.00"),
        true, true,
        "pisi-black-cat.jpg",
        List.of(
          new MedicalSeed("Intake check", today.minusMonths(1), "Vaccinations", "Underweight, gaining nicely", new BigDecimal("3.20"), false)
        )
      ),
    };
  }
}
