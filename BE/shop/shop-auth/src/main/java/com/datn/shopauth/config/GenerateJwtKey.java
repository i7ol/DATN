package com.datn.shopauth.config;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.util.Base64;
import javax.crypto.SecretKey;

public class GenerateJwtKey {
    public static void main(String[] args) {
        System.out.println("=== GENERATING JWT SECRET KEY ===");

        // Tạo key an toàn cho HS512
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        String base64Key = Base64.getEncoder().encodeToString(key.getEncoded());

        System.out.println(" SUCCESS! Copy this key to application.properties:");
        System.out.println("==========================================");
        System.out.println("jwt.secret=" + base64Key);
        System.out.println("==========================================");

        System.out.println(" Key Information:");
        System.out.println("- Characters: " + base64Key.length());
        System.out.println("- Bits: " + (key.getEncoded().length * 8));
        System.out.println("- Base64 Length: " + base64Key.length() + " chars");

        // Test key
        System.out.println("\n Testing key with sample JWT...");
        try {
            String testToken = Jwts.builder()
                    .setSubject("test")
                    .signWith(key, SignatureAlgorithm.HS512)
                    .compact();
            System.out.println("Test token generated: " + testToken.substring(0, 50) + "...");
            System.out.println("Key works correctly!");
        } catch (Exception e) {
            System.err.println(" Key test failed: " + e.getMessage());
        }
    }
}
