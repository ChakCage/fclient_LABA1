package ru.iu3.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.iu3.backend.models.Countries;
import ru.iu3.backend.repositories.CountryRepository;
import ru.iu3.backend.tools.DataValidationException;

import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/api/v1")

@CrossOrigin(origins = "http://localhost:3000")
public class CountryController {

    @Autowired
    CountryRepository countryRepository;

    @GetMapping("/countries")
    public Page<Countries> getAllCountries(@RequestParam("page") int page, @RequestParam("limit") int limit) {
        return countryRepository.findAll(PageRequest.of(page, limit, Sort.by(Sort.Direction.ASC, "name")));
    }
    @GetMapping("/allcountries")
    public List<Countries> getAllCountriesNoPagination() {
        // Просто возвращает все страны без пагинации
        return countryRepository.findAll();
    }
    @GetMapping("/countries/{id}")
    public ResponseEntity<Countries> getCountry(@PathVariable(value = "id") Long countryId)
            throws DataValidationException {
        Countries country = countryRepository.findById(countryId)
                .orElseThrow(() -> new DataValidationException("Страна с таким индексом не найдена"));
        return ResponseEntity.ok(country);
    }

    @PostMapping("/country/create")
    public ResponseEntity<Object> createCountryGet(@RequestParam("name") String name)
            throws DataValidationException {
        try {
            Countries country = new Countries();
            country.setName(name);
            Countries nc = countryRepository.save(country);
            return new ResponseEntity<Object>(nc, HttpStatus.OK);
        }
        catch(Exception ex) {
            if (ex.getMessage().contains("countries.name_UNIQUE"))
                throw new DataValidationException("Эта страна уже есть в базе");
            else
                throw new DataValidationException("Неизвестная ошибка");
        }
    }

    @PutMapping("/country/update/{id}")
    public ResponseEntity<Countries> updateCountryGet(
            @PathVariable(value = "id") Long countryId,
            @RequestParam("name") String name)
            throws DataValidationException {
        try {
            Countries country = countryRepository.findById(countryId)
                    .orElseThrow(() -> new DataValidationException("Страна с таким индексом не найдена"));
            country.setName(name);
            countryRepository.save(country);
            return ResponseEntity.ok(country);
        }
        catch (Exception ex) {
            if (ex.getMessage().contains("countries.name_UNIQUE"))
                throw new DataValidationException("Эта страна уже есть в базе");
            else
                throw new DataValidationException("Неизвестная ошибка");
        }
    }

    @PostMapping("/deletecountries")
    public ResponseEntity<?> deleteCountries(@Valid @RequestBody List<Countries> countries) {
        countryRepository.deleteAll(countries);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @CrossOrigin
    @DeleteMapping("/country/delete-by-id")
    public ResponseEntity<?> deleteCountriesById(@RequestParam("ids") List<Long> ids) {
        try {
            // Загружаем все страны по ID
            List<Countries> countries = new ArrayList<>();
            for (Long id : ids) {
                countryRepository.findById(id).ifPresent(countries::add);
            }

            // Удаляем загруженные страны
            if (!countries.isEmpty()) {
                countryRepository.deleteAll(countries);
            }

            return new ResponseEntity<>(HttpStatus.OK);
        } catch(Exception ex) {
            // Логирование ошибки для диагностики
            ex.printStackTrace();
            return new ResponseEntity<>(
                    Map.of("error", ex.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}