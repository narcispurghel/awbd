package com.github.irinabotea.webui.web;

import com.github.irinabotea.webui.client.AnimalServiceClient;
import com.github.irinabotea.webui.client.BackendDtos.AnimalDtos;
import com.github.irinabotea.webui.client.BackendException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

  private static final int CAROUSEL_LIMIT = 12;

  private final AnimalServiceClient animals;

  public HomeController(AnimalServiceClient animals) {
    this.animals = animals;
  }

  @GetMapping("/")
  public String index(Model model) {
    List<AnimalDtos.AnimalSummary> available = List.of();
    Map<UUID, UUID> firstPhotoByAnimal = new HashMap<>();
    try {
      List<AnimalDtos.AnimalSummary> all = animals.list(AnimalDtos.AnimalStatus.AVAILABLE, null, null);
      available = all.size() > CAROUSEL_LIMIT ? new ArrayList<>(all.subList(0, CAROUSEL_LIMIT)) : all;
      for (AnimalDtos.AnimalSummary a : available) {
        List<AnimalDtos.AnimalPhotoView> photos = animals.photos(a.id());
        if (!photos.isEmpty()) {
          firstPhotoByAnimal.put(a.id(), photos.get(0).id());
        }
      }
    } catch (BackendException ignored) {
      // catalog unreachable — render the page with no carousel
    }
    model.addAttribute("carouselAnimals", available);
    model.addAttribute("firstPhotoByAnimal", firstPhotoByAnimal);
    return "index";
  }
}
