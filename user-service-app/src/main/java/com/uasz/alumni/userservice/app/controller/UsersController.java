package com.uasz.alumni.userservice.app.controller;

import com.uasz.alumni.userservice.api.UsersApi;
import com.uasz.alumni.userservice.app.repository.UserRepository;
import com.uasz.alumni.userservice.model.CreateUserRequest;
import com.uasz.alumni.userservice.model.UpdateUserRequest;
import com.uasz.alumni.userservice.model.User;
import com.uasz.alumni.userservice.model.UserPage;
import com.uasz.alumni.userservice.model.UserStatus;
import com.uasz.alumni.userservice.model.UserType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Implementation Spring Boot du SPI genere par openapi-generator (UsersApi).
 * Toute la signature (routes, codes retour, validation) vient du fichier
 * user-service.yaml -> aucune divergence possible avec le contrat.
 */
@RestController
public class UsersController implements UsersApi {

    private final UserRepository repository;

    public UsersController(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public ResponseEntity<User> createUser(CreateUserRequest request) {
        repository.findByEmail(request.getEmail()).ifPresent(u -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Utilisateur deja existant");
        });

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setKeycloakId(request.getKeycloakId());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setType(request.getType() != null ? request.getType() : UserType.STUDENT);
        user.setStatus(UserStatus.ACTIVE);
        user.setAddress(request.getAddress());
        user.setCreatedAt(OffsetDateTime.now());

        repository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @Override
    public ResponseEntity<UserPage> searchUsers(@Nullable String name,
                                                 @Nullable String email,
                                                 @Nullable UserType type,
                                                 Integer page,
                                                 Integer size) {
        List<User> filtered = repository.findAll().stream()
                .filter(u -> name == null
                        || (u.getFirstName() + " " + u.getLastName()).toLowerCase().contains(name.toLowerCase()))
                .filter(u -> email == null || u.getEmail().equalsIgnoreCase(email))
                .filter(u -> type == null || u.getType() == type)
                .toList();

        int fromIndex = Math.min(page * size, filtered.size());
        int toIndex = Math.min(fromIndex + size, filtered.size());
        List<User> pageContent = filtered.subList(fromIndex, toIndex);

        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) filtered.size() / size);

        UserPage result = new UserPage(
                pageContent,
                page,
                size,
                filtered.size(),
                totalPages,
                page + 1 < totalPages,
                page > 0
        );
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<User> getUserById(UUID userId) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
        return ResponseEntity.ok(user);
    }

    @Override
    public ResponseEntity<User> updateUser(UUID userId, UpdateUserRequest request) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

        applyUpdate(user, request);
        repository.save(user);
        return ResponseEntity.ok(user);
    }

    @Override
    public ResponseEntity<Void> deleteUser(UUID userId) {
        if (!repository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable");
        }
        repository.deleteById(userId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<User> getCurrentUser() {
        // TODO: recuperer l'utilisateur courant depuis le token JWT (Keycloak) une fois la securite branchee
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Aucun utilisateur authentifie");
    }

    @Override
    public ResponseEntity<User> updateCurrentUser(UpdateUserRequest request) {
        // TODO: resoudre l'utilisateur courant depuis le token JWT (Keycloak)
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Aucun utilisateur authentifie");
    }

    private void applyUpdate(User user, UpdateUserRequest request) {
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getBiography() != null) {
            user.setBiography(request.getBiography());
        }
        if (request.getPhotoUrl() != null) {
            user.setPhotoUrl(request.getPhotoUrl());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }
        user.setUpdatedAt(OffsetDateTime.now());
    }
}
