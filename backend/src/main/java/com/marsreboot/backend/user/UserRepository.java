package com.marsreboot.backend.user;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/** Query akun ke database — Spring JPA generate SQL-nya otomatis dari nama method. */
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);
    boolean existsByUsername(String username);
}
