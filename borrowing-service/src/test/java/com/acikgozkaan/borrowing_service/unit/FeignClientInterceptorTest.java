package com.acikgozkaan.borrowing_service.unit;

import com.acikgozkaan.borrowing_service.config.FeignClientInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeignClientInterceptorTest {

    @Mock
    HttpServletRequest httpServletRequest;

    @Mock
    RequestTemplate requestTemplate;

    @BeforeEach
    void setup() {
        ServletRequestAttributes attributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(attributes);
    }

    @Test
    void shouldAddAuthorizationHeaderIfPresent() {
        when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer test-token");

        new FeignClientInterceptor()
                .requestInterceptor()
                .apply(requestTemplate);

        verify(requestTemplate).header("Authorization", "Bearer test-token");
    }

    @Test
    void shouldNotAddAuthorizationHeaderIfMissing() {
        when(httpServletRequest.getHeader("Authorization")).thenReturn(null);

        new FeignClientInterceptor()
                .requestInterceptor()
                .apply(requestTemplate);

        verify(requestTemplate, never()).header(anyString(), anyString());
    }
}