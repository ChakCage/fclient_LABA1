package ru.iu3.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.iu3.backend.models.Artist;
import ru.iu3.backend.models.Museum;
import ru.iu3.backend.models.Painting;
import ru.iu3.backend.repositories.ArtistRepository;
import ru.iu3.backend.repositories.CountryRepository;
import ru.iu3.backend.repositories.MuseumRepository;
import ru.iu3.backend.repositories.PaintingRepository;

import java.util.*;

@RestController
@RequestMapping("/api/v1")

@CrossOrigin(origins = "http://localhost:3000")
public class PaintingController {
    @Autowired
    private PaintingRepository paintingRepository;

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private MuseumRepository museumRepository;
    @GetMapping("/paintingsdetailed")
    public List<Map<String, Object>> getAllPaintingsDetailed() {
        List<Painting> paintings = paintingRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Painting painting : paintings) {
            Map<String, Object> paintingMap = new HashMap<>();
            paintingMap.put("id", painting.getId());
            paintingMap.put("name", painting.getName());
            paintingMap.put("year", painting.getYear());

            if (painting.getArtist() != null) {
                Map<String, Object> artistMap = new HashMap<>();
                artistMap.put("id", painting.getArtist().getId());
                artistMap.put("name", painting.getArtist().getName());
                paintingMap.put("artist", artistMap);
            }

            if (painting.getMuseum() != null) {
                Map<String, Object> museumMap = new HashMap<>();
                museumMap.put("id", painting.getMuseum().getId());
                museumMap.put("name", painting.getMuseum().getName());
                paintingMap.put("museum", museumMap);
            }

            result.add(paintingMap);
        }

        return result;
    }

    @PostMapping("/painting/create")
    public ResponseEntity<Object> createPaintingWithParams(
            @RequestParam("name") String name,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "artistId", required = false) Long artistId,
            @RequestParam(value = "museumId", required = false) Long museumId) {
        try {
            Painting painting = new Painting();
            painting.setName(name);

            if (year != null) {
                painting.setYear(year);
            }

            if (artistId != null) {
                Optional<Artist> artist = artistRepository.findById(artistId);
                if (!artist.isPresent()) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "artistnotfound");
                    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
                }
                painting.setArtist(artist.get());
            }

            if (museumId != null) {
                Optional<Museum> museum = museumRepository.findById(museumId);
                if (!museum.isPresent()) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "museumnotfound");
                    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
                }
                painting.setMuseum(museum.get());
            }

            Painting newPainting = paintingRepository.save(painting);
            return new ResponseEntity<>(newPainting, HttpStatus.OK);
        } catch (Exception ex) {
            String error;
            if (ex.getMessage().contains("paintings.name_UNIQUE")) {
                error = "paintingalreadyexists";
            } else {
                error = "undefinederror: " + ex.getMessage();
            }
            Map<String, String> map = new HashMap<>();
            map.put("error", error);
            return new ResponseEntity<>(map, HttpStatus.OK);
        }
    }

    @PutMapping("/painting/update/{id}")
    public ResponseEntity<Object> updatePaintingWithParams(
            @PathVariable(value = "id") Long paintingId,
            @RequestParam("name") String name,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "artistId", required = false) Long artistId,
            @RequestParam(value = "museumId", required = false) Long museumId) {
        try {
            Optional<Painting> paintingOpt = paintingRepository.findById(paintingId);

            if (!paintingOpt.isPresent()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Painting not found");
            }

            Painting painting = paintingOpt.get();
            painting.setName(name);

            if (year != null) {
                painting.setYear(year);
            }

            if (artistId != null) {
                Optional<Artist> artist = artistRepository.findById(artistId);
                if (!artist.isPresent()) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "artistnotfound");
                    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
                }
                painting.setArtist(artist.get());
            } else {
                painting.setArtist(null);
            }

            if (museumId != null) {
                Optional<Museum> museum = museumRepository.findById(museumId);
                if (!museum.isPresent()) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "museumnotfound");
                    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
                }
                painting.setMuseum(museum.get());
            } else {
                painting.setMuseum(null);
            }

            Painting updatedPainting = paintingRepository.save(painting);
            return ResponseEntity.ok(updatedPainting);
        } catch (Exception ex) {
            String error;
            if (ex.getMessage().contains("paintings.name_UNIQUE")) {
                error = "paintingalreadyexists";
            } else {
                error = "undefinederror: " + ex.getMessage();
            }
            Map<String, String> map = new HashMap<>();
            map.put("error", error);
            return new ResponseEntity<>(map, HttpStatus.OK);
        }
    }

    @DeleteMapping("/painting/delete-by-id")
    public ResponseEntity<?> deletePaintingsByIds(@RequestParam("ids") List<Long> ids) {
        List<Painting> paintings = new ArrayList<>();

        for (Long id : ids) {
            paintingRepository.findById(id).ifPresent(paintings::add);
        }

        if (!paintings.isEmpty()) {
            paintingRepository.deleteAll(paintings);
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(
                    Map.of("message", "Не найдены картины с указанными ID"),
                    HttpStatus.NOT_FOUND
            );
        }
    }
}
