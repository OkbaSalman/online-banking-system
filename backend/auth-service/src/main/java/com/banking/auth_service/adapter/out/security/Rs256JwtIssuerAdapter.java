package com.banking.auth_service.adapter.out.security;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;


import org.springframework.stereotype.Component;

import com.banking.auth_service.application.port.JwtIssuerPort;
import com.banking.auth_service.domain.model.Role;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;

@Component
public class Rs256JwtIssuerAdapter implements JwtIssuerPort {

    private final RSAPrivateKey privateKey;

    public Rs256JwtIssuerAdapter(@Value("${jwt.private-key-path}") String privateKeyPath) {
        this.privateKey = loadPrivateKeyFromPath(privateKeyPath);
    }

    @Override
    public JwtIssueResult issueAccessToken(UUID userId, Role role, Duration ttl) {
        Instant now = Instant.now();
        Instant exp = now.plus(ttl);

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(userId.toString())
                .issueTime(Date.from(now))
                .expirationTime(Date.from(exp))
                .claim("role", role.name())
                .build();

        SignedJWT jwt = new SignedJWT(
                new JWSHeader(JWSAlgorithm.RS256),
                claims
        );

        try {
            jwt.sign(new RSASSASigner(privateKey));
        } catch (JOSEException e) {
            throw new IllegalStateException("Failed to sign JWT", e);
        }

        return new JwtIssueResult(jwt.serialize(), ttl.toSeconds());
    }

    private static RSAPrivateKey loadPrivateKeyFromPath(String privateKeyPath) {
    try {
        String pem = Files.readString(Path.of(privateKeyPath));
        String base64 = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
 
        byte[] der = Base64.getDecoder().decode(base64);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(der);
 
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey key = kf.generatePrivate(spec);
        return (RSAPrivateKey) key;
    } catch (Exception e) {
        throw new IllegalStateException("Failed to load RSA private key from " + privateKeyPath, e);
    }
}
}