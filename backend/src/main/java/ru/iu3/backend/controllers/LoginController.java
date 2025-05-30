package ru.iu3.backend.controllers;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.iu3.backend.models.User;
import ru.iu3.backend.repositories.UserRepository;
import ru.iu3.backend.tools.Utils;

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/auth")
public class LoginController {
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody Map<String, String> credentials) {
        String login = credentials.get("login");
        String pwd = credentials.get("password");

        if (pwd != null && !pwd.isEmpty() && login != null && !login.isEmpty()) {
            User user = userRepository.findByLogin(login);

            if (user != null) {
                String hash1 = user.getPassword();
                String salt = user.getSalt();
                String hash2 = Utils.ComputeHash(pwd, salt);

                if (hash1.toLowerCase().equals(hash2.toLowerCase())) {
                    String token = UUID.randomUUID().toString();
                    user.setToken(token);
                    user.setActivity(new Date());
                    User savedUser = userRepository.saveAndFlush(user);
                    return new ResponseEntity<>(savedUser, HttpStatus.OK);
                }
            }
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @GetMapping("/logout")
    public ResponseEntity<Object> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token != null && !token.isEmpty()) {
            token = StringUtils.removeStart(token, "Bearer").trim();
            User user = userRepository.findByToken(token);
            if (user != null) {
                user.setToken(null);
                userRepository.save(user);
                return new ResponseEntity<>(HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
}