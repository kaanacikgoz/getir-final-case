package com.acikgozkaan.borrowing_service.client;

import com.acikgozkaan.borrowing_service.client.fallback.UserClientFallback;
import com.acikgozkaan.borrowing_service.config.FeignClientInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        name = "user-service",
        path = "api/v1/users",
        configuration = FeignClientInterceptor.class,
        fallback = UserClientFallback.class
)
public interface UserClient {

    @GetMapping("/{id}/check")
    void checkUserExists(@PathVariable("id") UUID id);
}