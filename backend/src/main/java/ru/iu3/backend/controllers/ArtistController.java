package ru.iu3.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.iu3.backend.models.Artist;
import ru.iu3.backend.models.Country;
import ru.iu3.backend.repositories.ArtistRepository;
import ru.iu3.backend.repositories.CountryRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/v1")
public class ArtistController {

    @Autowired
    ArtistRepository artistRepository;

    @Autowired
    CountryRepository countryRepository;

    @GetMapping("/artists")
    public List
    getAllArtists() {
        return artistRepository.findAll();
    }

    @PostMapping("/artists")
    public ResponseEntity<Object> createArtist(@RequestBody Artist artist)
            throws Exception {
        try {
            Optional<Country>
                    cc = countryRepository.findById(artist.country.id);
            if (cc.isPresent()) {
                artist.country = cc.get();
            }
            Artist newArtist = artistRepository.save(artist);
            return new ResponseEntity<Object>(newArtist, HttpStatus.OK);
        }
        catch(Exception ex) {
            String error;
            if (ex.getMessage().contains("countries.name_UNIQUE"))
                error = "artist_already_exists";
            else
                error = "undefined_error";
            Map<String, String>
                    map =  new HashMap<>();
            map.put("error", error);
            return ResponseEntity.ok(map);
        }
    }

    @DeleteMapping("/artists/{id}")
    public ResponseEntity<Object> deleteArtist(@PathVariable(value = "id") Long artistId) {
        Optional<Artist>
                artist = artistRepository.findById(artistId);
        Map<String, Boolean>
                resp = new HashMap<>();
        if (artist.isPresent()) {
            artistRepository.delete(artist.get());
            resp.put("deleted", Boolean.TRUE);
        }
        else
            resp.put("deleted", Boolean.FALSE);
        return ResponseEntity.ok(resp);
    }
}