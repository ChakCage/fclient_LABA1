package ru.iu3.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.iu3.backend.models.Country;
import ru.iu3.backend.models.Museum;
import ru.iu3.backend.models.Painting;
import ru.iu3.backend.models.User;
import ru.iu3.backend.repositories.MuseumRepository;
import ru.iu3.backend.repositories.PaintingRepository;

import java.util.*;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/v1")
public class MuseumController {

    @Autowired
    MuseumRepository museumRepository;

    @Autowired
    PaintingRepository paintingRepository;

    @GetMapping("/museums")
    public List
    getAllMuseums() {
        return museumRepository.findAll();
    }

    @PostMapping("/museums")
    public ResponseEntity<Object> createMuseum(@RequestBody Museum museum)
            throws Exception {
        try {
            Museum newMuseum = museumRepository.save(museum);
            return new ResponseEntity<Object>(newMuseum, HttpStatus.OK);
        }
        catch(Exception ex) {
            String error;
            if (ex.getMessage().contains("museums.name_UNIQUE"))
                error = "museum_already_exists";
            else
                error = "undefined_error";
            Map<String, String>
                    map =  new HashMap<>();
            map.put("error", error);
            return ResponseEntity.ok(map);
        }
    }

}