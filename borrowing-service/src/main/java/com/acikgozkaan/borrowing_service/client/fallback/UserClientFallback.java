package com.acikgozkaan.borrowing_service.client.fallback;

import com.acikgozkaan.borrowing_service.client.UserClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class UserClientFallback implements UserClient {
    @Override
    public void checkUserExists(UUID id) {
        log.warn("Fallback: user-service not available.");
    }
}
