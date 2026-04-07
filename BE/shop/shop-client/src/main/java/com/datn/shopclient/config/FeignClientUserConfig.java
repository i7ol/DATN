package com.datn.shopclient.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignClientUserConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attrs != null) {
                String authHeader = attrs.getRequest().getHeader("Authorization");

                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    requestTemplate.header("Authorization", authHeader);
                    System.out.println(">>> TOKEN forwarded (Bearer): " +
                            authHeader.substring(0, Math.min(50, authHeader.length())) + "...");
                } else {
                    System.out.println(">>> No Bearer token - Guest or internal call");
                }
            } else {
                System.out.println(">>> No RequestContext - Internal call or scheduler");
            }
        };
    }
}