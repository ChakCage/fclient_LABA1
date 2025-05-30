package ru.iu3.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.iu3.backend.models.Museum;
import ru.iu3.backend.models.User;
import ru.iu3.backend.repositories.MuseumRepository;
import ru.iu3.backend.repositories.UserRepository;
import ru.iu3.backend.tools.Utils;

import java.util.*;


@RestController
@RequestMapping("/api/v1")

@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MuseumRepository museumRepository;

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable(value = "id") Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return ResponseEntity.ok(user);
    }

    @PostMapping("/users")
    public ResponseEntity<Object> createUser(@RequestBody User user) {
        try {
            // Генерация соли
            String salt = UUID.randomUUID().toString().substring(0, 8);
            user.setSalt(salt);

            // Шифрование пароля
            String rawPassword = user.getPassword();
            if (rawPassword != null && !rawPassword.isEmpty()) {
                String hashedPassword = Utils.ComputeHash(rawPassword, salt);
                user.setPassword(hashedPassword);
            }

            User newUser = userRepository.save(user);
            return new ResponseEntity<>(newUser, HttpStatus.OK);
        } catch (Exception ex) {
            Map<String, String> error = new HashMap<>();
            if (ex.getMessage().contains("users.login_UNIQUE")) {
                error.put("error", "loginexists");
            } else if (ex.getMessage().contains("users.email_UNIQUE")) {
                error.put("error", "emailexists");
            } else {
                error.put("error", "undefinederror");
            }
            return new ResponseEntity<>(error, HttpStatus.OK);
        }
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<Object> updateUser(@PathVariable(value = "id") Long userId,
                                             @RequestBody User userDetails) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        user.setLogin(userDetails.getLogin());
        user.setEmail(userDetails.getEmail());
        // Обновляем только если пароль был предоставлен
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(userDetails.getPassword());
        }
        user.setSalt(userDetails.getSalt());
        user.setToken(userDetails.getToken());
        user.setActivity(userDetails.getActivity());

        try {
            User updatedUser = userRepository.save(user);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception ex) {
            Map<String, String> error = new HashMap<>();
            if (ex.getMessage().contains("users.login_UNIQUE")) {
                error.put("error", "loginexists");
            } else if (ex.getMessage().contains("users.email_UNIQUE")) {
                error.put("error", "emailexists");
            } else {
                error.put("error", "undefinederror");
            }
            return new ResponseEntity<>(error, HttpStatus.OK);
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Object> deleteUser(@PathVariable(value = "id") Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        userRepository.delete(user);

        Map<String, Boolean> response = new HashMap<>();
        response.put("deleted", Boolean.TRUE);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/users/{id}/museums")
    public Set<Museum> getUserMuseums(@PathVariable(value = "id") Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return user.getMuseums();
    }

    @PutMapping("/users/{userId}/museums/{museumId}")
    public ResponseEntity<Object> addMuseumToUser(
            @PathVariable(value = "userId") Long userId,
            @PathVariable(value = "museumId") Long museumId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Museum museum = museumRepository.findById(museumId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Museum not found"));

        user.getMuseums().add(museum);
        userRepository.save(user);

        Map<String, Boolean> response = new HashMap<>();
        response.put("added", Boolean.TRUE);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/users/{userId}/museums/{museumId}")
    public ResponseEntity<Object> removeMuseumFromUser(
            @PathVariable(value = "userId") Long userId,
            @PathVariable(value = "museumId") Long museumId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Museum museum = museumRepository.findById(museumId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Museum not found"));

        user.getMuseums().remove(museum);
        userRepository.save(user);

        Map<String, Boolean> response = new HashMap<>();
        response.put("removed", Boolean.TRUE);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/usersdetailed")
    public List<Map<String, Object>> getAllUsersDetailed() {
        List<User> users = userRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (User user : users) {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("login", user.getLogin());
            userMap.put("email", user.getEmail());

            // Дата последней активности
            if (user.getActivity() != null) {
                userMap.put("activity", user.getActivity());
            }

            // Если нужно показать связанные музеи
            if (user.getMuseums() != null && !user.getMuseums().isEmpty()) {
                List<Map<String, Object>> museumsList = new ArrayList<>();
                for (Museum museum : user.getMuseums()) {
                    Map<String, Object> museumMap = new HashMap<>();
                    museumMap.put("id", museum.getId());
                    museumMap.put("name", museum.getName());
                    museumsList.add(museumMap);
                }
                userMap.put("museums", museumsList);
            }

            result.add(userMap);
        }

        return result;
    }

    @PostMapping("/user/create")
    public ResponseEntity<Object> createUserWithParams(
            @RequestParam("login") String login,
            @RequestParam("email") String email,
            @RequestParam("password") String password) {
        try {
            User user = new User();
            user.setLogin(login);
            user.setEmail(email);

            // Генерация соли и хеширование пароля
            byte[] b = new byte[32];
            new Random().nextBytes(b);
            String salt = new String(Hex.encode(b));
            user.setPassword(Utils.ComputeHash(password, salt));
            user.setSalt(salt);

            // Устанавливаем текущее время активности
            user.setActivity(new Date());

            User newUser = userRepository.save(user);
            return new ResponseEntity<>(newUser, HttpStatus.OK);
        } catch (Exception ex) {
            String error;
            if (ex.getMessage().contains("users.login_UNIQUE")) {
                error = "useralreadyexists";
            } else if (ex.getMessage().contains("users.email_UNIQUE")) {
                error = "emailalreadyexists";
            } else {
                error = "undefinederror: " + ex.getMessage();
            }
            Map<String, String> map = new HashMap<>();
            map.put("error", error);
            return new ResponseEntity<>(map, HttpStatus.OK);
        }
    }

    @PutMapping("/user/update/{id}")
    public ResponseEntity<Object> updateUserWithParams(
            @PathVariable(value = "id") Long userId,
            @RequestParam("login") String login,
            @RequestParam("email") String email,
            @RequestParam(value = "password", required = false) String password) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);

            if (!userOpt.isPresent()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
            }

            User user = userOpt.get();
            user.setLogin(login);
            user.setEmail(email);

            // Если указан новый пароль, хешируем его
            if (password != null && !password.isEmpty()) {
                byte[] b = new byte[32];
                new Random().nextBytes(b);
                String salt = new String(Hex.encode(b));
                user.setPassword(Utils.ComputeHash(password, salt));
                user.setSalt(salt);
            }

            // Обновляем время активности
            user.setActivity(new Date());

            User updatedUser = userRepository.save(user);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception ex) {
            String error;
            if (ex.getMessage().contains("users.login_UNIQUE")) {
                error = "useralreadyexists";
            } else if (ex.getMessage().contains("users.email_UNIQUE")) {
                error = "emailalreadyexists";
            } else {
                error = "undefinederror: " + ex.getMessage();
            }
            Map<String, String> map = new HashMap<>();
            map.put("error", error);
            return new ResponseEntity<>(map, HttpStatus.OK);
        }
    }

    @DeleteMapping("/user/delete-by-id")
    public ResponseEntity<?> deleteUsersByIds(@RequestParam("ids") List<Long> ids) {
        List<User> users = new ArrayList<>();

        for (Long id : ids) {
            userRepository.findById(id).ifPresent(users::add);
        }

        if (!users.isEmpty()) {
            userRepository.deleteAll(users);
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(
                    Map.of("message", "Не найдены пользователи с указанными ID"),
                    HttpStatus.NOT_FOUND
            );
        }
    }

    // Метод для управления связью между пользователем и музеями
    @PostMapping("/user/{userId}/addmuseums")
    public ResponseEntity<?> addMuseumsToUser(
            @PathVariable(value = "id") Long userId,
            @RequestParam("museumIds") List<Long> museumIds) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<Museum> museumsToAdd = new ArrayList<>();
        for (Long museumId : museumIds) {
            museumRepository.findById(museumId).ifPresent(museumsToAdd::add);
        }

        // Добавляем музеи к текущему списку музеев пользователя
        if (user.getMuseums() == null) {
            user.setMuseums(new HashSet<>());
        }
        user.getMuseums().addAll(museumsToAdd);

        userRepository.save(user);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/user/{userId}/removemuseums")
    public ResponseEntity<?> removeMuseumsFromUser(
            @PathVariable(value = "id") Long userId,
            @RequestParam("museumIds") List<Long> museumIds) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getMuseums() != null) {
            user.getMuseums().removeIf(museum -> museumIds.contains(museum.getId()));
            userRepository.save(user);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }
}