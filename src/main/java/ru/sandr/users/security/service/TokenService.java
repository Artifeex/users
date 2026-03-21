package ru.sandr.users.security.service;

import java.util.Optional;

public interface TokenService {

    void generateAndSaveResetPasswordTokenForEmail(String email);

    Optional<String> getResetPasswordTokenByEmail(String email);
}
