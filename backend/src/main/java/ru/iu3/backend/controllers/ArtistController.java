package ru.iu3.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.iu3.backend.models.Artist;
import ru.iu3.backend.models.Countries;
import ru.iu3.backend.repositories.ArtistRepository;
import ru.iu3.backend.repositories.CountryRepository;

import java.util.*;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "http://localhost:3000")
public class ArtistController {

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private CountryRepository countryRepository;

    @GetMapping("/artists")
    public Page<Artist> getAllArtists(@RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int limit) {
        return artistRepository.findAll(PageRequest.of(page, limit, Sort.by(Sort.Direction.ASC, "name")));
    }

    @GetMapping("/artists/{id}")
    public ResponseEntity<Artist> getArtistById(@PathVariable(value = "id") Long artistId) {
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Artist not found"));
        return ResponseEntity.ok(artist);
    }

    @PostMapping("/artists")
    public ResponseEntity<Object> createArtist(@RequestBody Artist artist) {
        try {

            if (artist.getCountry() != null && artist.getCountry().getId() != null) {
                Optional<Countries> country = countryRepository.findById(artist.getCountry().getId());
                if (!country.isPresent()) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "countrynotfound");
                    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
                }
                artist.setCountry(country.get());
            }

            Artist newArtist = artistRepository.save(artist);
            return new ResponseEntity<>(newArtist, HttpStatus.OK);
        } catch (Exception ex) {
            String error;
            if (ex.getMessage().contains("artists.name_UNIQUE")) {
                error = "artistalreadyexists";
            } else {
                error = "undefinederror";
            }
            Map<String, String> map = new HashMap<>();
            map.put("error", error);
            return new ResponseEntity<>(map, HttpStatus.OK);
        }
    }

    @PutMapping("/artists/{id}")
    public ResponseEntity<Object> updateArtist(@PathVariable(value = "id") Long artistId,
                                               @RequestBody Artist artistDetails) {
        Optional<Artist> artistOpt = artistRepository.findById(artistId);

        if (!artistOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Artist not found");
        }

        Artist artist = artistOpt.get();


        artist.setName(artistDetails.getName());
        artist.setAge(artistDetails.getAge());


        if (artistDetails.getCountry() != null && artistDetails.getCountry().getId() != null) {
            Optional<Countries> country = countryRepository.findById(artistDetails.getCountry().getId());
            if (!country.isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "countrynotfound");
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
            }
            artist.setCountry(country.get());
        }

        Artist updatedArtist = artistRepository.save(artist);
        return ResponseEntity.ok(updatedArtist);
    }

    @DeleteMapping("/artists/{id}")
    public ResponseEntity<Object> deleteArtist(@PathVariable(value = "id") Long artistId) {
        Optional<Artist> artist = artistRepository.findById(artistId);
        Map<String, Boolean> resp = new HashMap<>();

        if (artist.isPresent()) {
            artistRepository.delete(artist.get());
            resp.put("deleted", Boolean.TRUE);
        } else {
            resp.put("deleted", Boolean.FALSE);
        }

        return ResponseEntity.ok(resp);
    }


    @GetMapping("/countries/{countryId}/artists")
    public List<Artist> getArtistsByCountry(@PathVariable(value = "countryId") Long countryId) {
        Countries country = countryRepository.findById(countryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Country not found"));
        return artistRepository.findByCountry(country);
    }

    @GetMapping("/artistsdetailed")
    public List<Map<String, Object>> getAllArtistsDetailed() {
        List<Artist> artists = artistRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Artist artist : artists) {
            Map<String, Object> artistMap = new HashMap<>();
            artistMap.put("id", artist.getId());
            artistMap.put("name", artist.getName());
            artistMap.put("age", artist.getAge());

            if (artist.getCountry() != null) {
                Map<String, Object> countryMap = new HashMap<>();
                countryMap.put("id", artist.getCountry().getId());
                countryMap.put("name", artist.getCountry().getName());
                artistMap.put("country", countryMap);
            }

            result.add(artistMap);
        }

        return result;
    }

    @PostMapping("/artist/create")
    public ResponseEntity<Object> createArtistWithParams(
            @RequestParam("name") String name,
            @RequestParam(value = "age", required = false) String age,
            @RequestParam(value = "countryId", required = false) Long countryId) {
        try {
            Artist artist = new Artist();
            artist.setName(name);

            if (age != null) {
                artist.setAge(age);
            }

            if (countryId != null) {
                Optional<Countries> country = countryRepository.findById(countryId);
                if (!country.isPresent()) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "countrynotfound");
                    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
                }
                artist.setCountry(country.get());
            }

            Artist newArtist = artistRepository.save(artist);
            return new ResponseEntity<>(newArtist, HttpStatus.OK);
        } catch (Exception ex) {
            String error;
            if (ex.getMessage().contains("artists.name_UNIQUE")) {
                error = "artistalreadyexists";
            } else {
                error = "undefinederror: " + ex.getMessage();
            }
            Map<String, String> map = new HashMap<>();
            map.put("error", error);
            return new ResponseEntity<>(map, HttpStatus.OK);
        }
    }

    @PutMapping("/artist/update/{id}")
    public ResponseEntity<Object> updateArtistWithParams(
            @PathVariable(value = "id") Long artistId,
            @RequestParam("name") String name,
            @RequestParam(value = "age", required = false) String age,
            @RequestParam(value = "countryId", required = false) Long countryId) {
        try {
            Optional<Artist> artistOpt = artistRepository.findById(artistId);

            if (!artistOpt.isPresent()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Artist not found");
            }

            Artist artist = artistOpt.get();
            artist.setName(name);

            if (age != null) {
                artist.setAge(age);
            }

            if (countryId != null) {
                Optional<Countries> country = countryRepository.findById(countryId);
                if (!country.isPresent()) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "countrynotfound");
                    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
                }
                artist.setCountry(country.get());
            } else {
                // Если countryId не указан, убираем связь с country
                artist.setCountry(null);
            }

            Artist updatedArtist = artistRepository.save(artist);
            return ResponseEntity.ok(updatedArtist);
        } catch (Exception ex) {
            String error;
            if (ex.getMessage().contains("artists.name_UNIQUE")) {
                error = "artistalreadyexists";
            } else {
                error = "undefinederror: " + ex.getMessage();
            }
            Map<String, String> map = new HashMap<>();
            map.put("error", error);
            return new ResponseEntity<>(map, HttpStatus.OK);
        }
    }

    @DeleteMapping("/artist/delete-by-id")
    public ResponseEntity<?> deleteArtistsByIds(@RequestParam("ids") List<Long> ids) {
        List<Artist> artists = new ArrayList<>();

        for (Long id : ids) {
            artistRepository.findById(id).ifPresent(artists::add);
        }

        if (!artists.isEmpty()) {
            artistRepository.deleteAll(artists);
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(
                    Map.of("message", "Не найдены художники с указанными ID"),
                    HttpStatus.NOT_FOUND
            );
        }
    }
}