package ru.sandr.users.security.controller;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import io.jsonwebtoken.security.JwkSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.interfaces.RSAPublicKey;
import java.util.Collections;
import java.util.Map;

@RestController
public class JwksController {

    @Value("${tokens.jwt.public-key}")
    private RSAPublicKey publicKey;

    @Value("${tokens.jwt.kid}")
    private String kid;

    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> getJwks() {
        RSAKey jwk = new RSAKey.Builder(publicKey)
                .keyUse(KeyUse.SIGNATURE) // ключ используется для подписи
                .algorithm(JWSAlgorithm.RS256)
                .keyID(kid) // Заголовок ключа
                .build();

        return new JWKSet(jwk).toJSONObject();
    }
}
