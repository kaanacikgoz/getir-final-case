package com.acikgozkaan.user_service.init;

import com.acikgozkaan.user_service.entity.Role;
import com.acikgozkaan.user_service.entity.User;
import com.acikgozkaan.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class FirstLibrarian {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initLibrarianUser() {
        return args -> {
            String librarianEmail = "admin@getir.com";
            String librarianPhone = "123123";

            if (!userRepository.existsByEmail(librarianEmail)
                && !userRepository.existsByPhone(librarianPhone)) {
                User librarian = User.builder()
                        .email(librarianEmail)
                        .password(passwordEncoder.encode("123456"))
                        .role(Role.LIBRARIAN)
                        .name("Admin")
                        .surname("User")
                        .phone(librarianPhone)
                        .address("Default Library Address")
                        .build();

                userRepository.save(librarian);
                log.info("Default librarian user created: {}", librarianEmail);
            }
        };
    }
}