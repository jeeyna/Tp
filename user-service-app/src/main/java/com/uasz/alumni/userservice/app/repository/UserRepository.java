package com.uasz.alumni.userservice.app.repository;

import com.uasz.alumni.userservice.model.User;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Stockage en memoire, juste pour faire tourner/demontrer le SPI.
 * A remplacer par un vrai repository JPA + PostgreSQL.
 */
@Repository
public class UserRepository {

    private final Map<UUID, User> store = new LinkedHashMap<>();

    public User save(User user) {
        store.put(user.getId(), user);
        return user;
    }

    public Optional<User> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    public Optional<User> findByEmail(String email) {
        return store.values().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    public Collection<User> findAll() {
        return store.values();
    }

    public void deleteById(UUID id) {
        store.remove(id);
    }

    public boolean existsById(UUID id) {
        return store.containsKey(id);
    }
}
