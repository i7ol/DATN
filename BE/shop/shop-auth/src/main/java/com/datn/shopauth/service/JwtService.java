package com.datn.shopauth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKeyString;

    @Value("${jwt.expiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-expiration}")
    private Long refreshTokenExpiration;

    private final RedisTemplate<String, Object> redisTemplate;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        try {
            log.info("Initializing JWT Service...");

            initializeSecretKey();

            log.info("JWT Service initialized successfully");
            log.info("Access token expiration: {} ms", accessTokenExpiration);
            log.info("Refresh token expiration: {} ms", refreshTokenExpiration);

        } catch (Exception e) {
            log.error("Failed to initialize JWT Service", e);
            throw new RuntimeException("JWT Service initialization failed", e);
        }
    }

    private void initializeSecretKey() {
        try {
            if (secretKeyString == null || secretKeyString.trim().isEmpty()) {
                log.warn("JWT secret is empty in configuration");
                generateAndSetNewKey();
                return;
            }

            // Decode từ Base64
            byte[] decodedKey;
            try {
                decodedKey = Base64.getDecoder().decode(secretKeyString.trim());
            } catch (IllegalArgumentException e) {
                log.error("Invalid Base64 format for JWT secret", e);
                generateAndSetNewKey();
                return;
            }


            int bitLength = decodedKey.length * 8;
            log.info("JWT secret key bit length: {} bits", bitLength);

            if (bitLength < 512) {
                log.warn("JWT secret key is too short for HS512 algorithm!");
                log.warn("Current length: {} bits, Required: >= 512 bits", bitLength);
                log.warn("Generating a new secure key...");
                generateAndSetNewKey();
            } else {
                // Tạo SecretKey từ bytes
                this.secretKey = Keys.hmacShaKeyFor(decodedKey);
                log.info("JWT secret key loaded successfully ({} bits)", bitLength);
            }

        } catch (Exception e) {
            log.error("Error initializing JWT secret key", e);
            generateAndSetNewKey();
        }
    }

    private void generateAndSetNewKey() {
        try {
            // Tạo key mới an toàn cho HS512
            this.secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
            String newBase64Key = Base64.getEncoder().encodeToString(secretKey.getEncoded());
            int bitLength = secretKey.getEncoded().length * 8;

            log.warn("Please update your application.properties with this key:");
            log.warn("");
            log.warn("jwt.secret={}", newBase64Key);
            log.warn("");
            log.warn("Key information:");
            log.warn("- Length: {} characters", newBase64Key.length());
            log.warn("- Bit size: {} bits (secure for HS512)", bitLength);
            log.warn("Without updating, tokens will be invalid after restart!");

        } catch (Exception e) {
            log.error("Failed to generate new JWT key", e);
            throw new RuntimeException("Cannot generate JWT secret key", e);
        }
    }

    // ==================== ACCESS TOKEN ====================
    public String generateAccessToken(String username, List<String> roles, List<String> permissions) {
        return generateToken(username, roles, permissions, accessTokenExpiration, "ACCESS");
    }

    // ==================== REFRESH TOKEN ====================
    public String generateRefreshToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "REFRESH");

        String refreshToken = Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(secretKey, SignatureAlgorithm.HS512)  // Sử dụng secretKey object
                .compact();

        // Lưu refresh token vào Redis với TTL
        String redisKey = "refresh_token:" + username;
        redisTemplate.opsForValue().set(
                redisKey,
                refreshToken,
                refreshTokenExpiration,
                TimeUnit.MILLISECONDS
        );

        log.info("Refresh token stored in Redis for user: {}", username);
        return refreshToken;
    }

    private String generateToken(String username, List<String> roles, List<String> permissions,
                                 Long expiration, String tokenType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);
        claims.put("permissions", permissions);
        claims.put("type", tokenType);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secretKey, SignatureAlgorithm.HS512)  // Sử dụng secretKey object
                .compact();
    }

    // ==================== TOKEN VALIDATION ====================
    public boolean validateToken(String token) {
        try {
            // 1. Kiểm tra token có trong blacklist không
            if (isTokenBlacklisted(token)) {
                log.warn("Token is blacklisted: {}", token.substring(0, 20) + "...");
                return false;
            }

            // 2. Validate JWT signature và expiration
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)  // Sử dụng secretKey object
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException e) {
            log.error("JWT Security exception: {}", e.getMessage());
            return false;
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            log.error("Malformed JWT token: {}", e.getMessage());
            return false;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("JWT token expired: {}", e.getMessage());
            return false;
        } catch (io.jsonwebtoken.UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public boolean validateRefreshToken(String refreshToken) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)  // Sử dụng secretKey object
                    .build()
                    .parseClaimsJws(refreshToken)
                    .getBody();

            String username = claims.getSubject();
            String tokenType = claims.get("type", String.class);

            if (!"REFRESH".equals(tokenType)) {
                log.warn("Invalid token type for refresh token: {}", tokenType);
                return false;
            }

            // Kiểm tra refresh token có trong Redis không
            String redisKey = "refresh_token:" + username;
            String storedToken = (String) redisTemplate.opsForValue().get(redisKey);

            if (storedToken == null) {
                log.warn("Refresh token not found in Redis for user: {}", username);
                return false;
            }

            boolean isValid = refreshToken.equals(storedToken);
            if (!isValid) {
                log.warn("Refresh token mismatch for user: {}", username);
            }
            return isValid;
        } catch (Exception e) {
            log.error("Refresh token validation failed: {}", e.getMessage());
            return false;
        }
    }

    // ==================== TOKEN BLACKLIST ====================
    public void blacklistToken(String token, long remainingTime) {
        if (remainingTime > 0) {
            String key = "blacklist:" + token;
            redisTemplate.opsForValue().set(
                    key,
                    "blacklisted",
                    remainingTime,
                    TimeUnit.MILLISECONDS
            );
            log.info("Token blacklisted with TTL: {} ms", remainingTime);
        } else {
            log.warn("Cannot blacklist token with non-positive remaining time: {}", remainingTime);
        }
    }

    public boolean isTokenBlacklisted(String token) {
        String key = "blacklist:" + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    // ==================== REFRESH TOKEN MANAGEMENT ====================
    public void invalidateRefreshToken(String username) {
        String redisKey = "refresh_token:" + username;
        Boolean deleted = redisTemplate.delete(redisKey);
        if (Boolean.TRUE.equals(deleted)) {
            log.info("Refresh token invalidated for user: {}", username);
        } else {
            log.warn("Refresh token not found for user: {}", username);
        }
    }

    // ==================== TOKEN INFO EXTRACTION ====================
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public List<String> extractRoles(String token) {
        return extractAllClaims(token).get("roles", List.class);
    }

    public List<String> extractPermissions(String token) {
        return extractAllClaims(token).get("permissions", List.class);
    }

    public String extractTokenType(String token) {
        return extractAllClaims(token).get("type", String.class);
    }

    public Long getRemainingTime(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Date expiration = claims.getExpiration();
            return expiration.getTime() - System.currentTimeMillis();
        } catch (Exception e) {
            log.error("Failed to get remaining time for token: {}", e.getMessage());
            return 0L;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)  // Sử dụng secretKey object
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // ==================== UTILITY METHODS ====================
    public void storeUserSession(String username, String sessionData) {
        String key = "user_session:" + username;
        redisTemplate.opsForValue().set(
                key,
                sessionData,
                accessTokenExpiration,  // Same as access token expiration
                TimeUnit.MILLISECONDS
        );
        log.debug("User session stored for: {}", username);
    }

    public String getUserSession(String username) {
        String key = "user_session:" + username;
        return (String) redisTemplate.opsForValue().get(key);
    }

    public void invalidateUserSession(String username) {
        String key = "user_session:" + username;
        Boolean deleted = redisTemplate.delete(key);
        if (Boolean.TRUE.equals(deleted)) {
            log.debug("User session invalidated for: {}", username);
        }
    }

    // ==================== BATCH OPERATIONS ====================
    public void invalidateAllUserTokens(String username) {
        // Xóa refresh token
        invalidateRefreshToken(username);

        // Xóa user session
        invalidateUserSession(username);

        log.info("All tokens invalidated for user: {}", username);
    }

    // ==================== DEBUG/HELPER METHODS ====================
    public String getCurrentKeyInfo() {
        if (secretKey == null) {
            return "Secret key is not initialized";
        }

        byte[] keyBytes = secretKey.getEncoded();
        return String.format("Key size: %d bits, Algorithm: %s",
                keyBytes.length * 8, secretKey.getAlgorithm());
    }
}