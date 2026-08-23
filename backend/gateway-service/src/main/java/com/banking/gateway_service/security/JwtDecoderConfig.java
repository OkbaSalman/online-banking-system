package com.banking.gateway_service.security;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

@Configuration
public class JwtDecoderConfig {

    @Bean
    ReactiveJwtDecoder jwtDecoder(@Value("${jwt.public-key-path}") String publicKeyPath) {
        RSAPublicKey publicKey = loadPublicKeyFromPath(publicKeyPath);
        return NimbusReactiveJwtDecoder.withPublicKey(publicKey).build();
    }

    private static RSAPublicKey loadPublicKeyFromPath(String publicKeyPath) {
        try {
            String pem = Files.readString(Path.of(publicKeyPath));
            String base64 = pem
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] der = Base64.getDecoder().decode(base64);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(der);

            KeyFactory kf = KeyFactory.getInstance("RSA");
            PublicKey key = kf.generatePublic(spec);
            return (RSAPublicKey) key;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load RSA public key from " + publicKeyPath, e);
        }
    }
}