package com.acikgozkaan.auth_service.repository;

import com.acikgozkaan.auth_service.entity.UserCredential;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserCredentialRepository extends CrudRepository<UserCredential, UUID> {

    Optional<UserCredential> findByEmail(String email);
    boolean existsByEmail(String email);
}