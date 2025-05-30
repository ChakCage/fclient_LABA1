package ru.iu3.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.iu3.backend.models.Museum;
import ru.iu3.backend.repositories.MuseumRepository;

import java.util.*;

@RestController
@RequestMapping("/api/v1")

@CrossOrigin(origins = "http://localhost:3000")
public class MuseumController {

    @Autowired
    private MuseumRepository museumRepository;

    @GetMapping("/museumsdetailed")
    public List<Map<String, Object>> getAllMuseumsDetailed() {
        List<Museum> museums = museumRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Museum museum : museums) {
            Map<String, Object> museumMap = new HashMap<>();
            museumMap.put("id", museum.getId());
            museumMap.put("name", museum.getName());
            museumMap.put("location", museum.getLocation());

            // Можно также добавить количество картин в музее
            if (museum.getPaintings() != null) {
                museumMap.put("paintingsCount", museum.getPaintings().size());
            }

            result.add(museumMap);
        }

        return result;
    }

    @PostMapping("/museum/create")
    public ResponseEntity<Object> createMuseumWithParams(
            @RequestParam("name") String name,
            @RequestParam(value = "location", required = false) String location) {
        try {
            Museum museum = new Museum();
            museum.setName(name);

            if (location != null) {
                museum.setLocation(location);
            }

            Museum newMuseum = museumRepository.save(museum);
            return new ResponseEntity<>(newMuseum, HttpStatus.OK);
        } catch (Exception ex) {
            String error;
            if (ex.getMessage().contains("museums.name_UNIQUE")) {
                error = "museumalreadyexists";
            } else {
                error = "undefinederror: " + ex.getMessage();
            }
            Map<String, String> map = new HashMap<>();
            map.put("error", error);
            return new ResponseEntity<>(map, HttpStatus.OK);
        }
    }

    @PutMapping("/museum/update/{id}")
    public ResponseEntity<Object> updateMuseumWithParams(
            @PathVariable(value = "id") Long museumId,
            @RequestParam("name") String name,
            @RequestParam(value = "location", required = false) String location) {
        try {
            Optional<Museum> museumOpt = museumRepository.findById(museumId);

            if (!museumOpt.isPresent()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Museum not found");
            }

            Museum museum = museumOpt.get();
            museum.setName(name);

            if (location != null) {
                museum.setLocation(location);
            }

            Museum updatedMuseum = museumRepository.save(museum);
            return ResponseEntity.ok(updatedMuseum);
        } catch (Exception ex) {
            String error;
            if (ex.getMessage().contains("museums.name_UNIQUE")) {
                error = "museumalreadyexists";
            } else {
                error = "undefinederror: " + ex.getMessage();
            }
            Map<String, String> map = new HashMap<>();
            map.put("error", error);
            return new ResponseEntity<>(map, HttpStatus.OK);
        }
    }

    @DeleteMapping("/museum/delete-by-id")
    public ResponseEntity<?> deleteMuseumsByIds(@RequestParam("ids") List<Long> ids) {
        List<Museum> museums = new ArrayList<>();

        for (Long id : ids) {
            museumRepository.findById(id).ifPresent(museums::add);
        }

        if (!museums.isEmpty()) {
            museumRepository.deleteAll(museums);
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(
                    Map.of("message", "Не найдены музеи с указанными ID"),
                    HttpStatus.NOT_FOUND
            );
        }
    }


}