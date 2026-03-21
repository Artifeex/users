package ru.sandr.users.security.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sandr.users.security.service.TokenService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {


    @Override
    public void generateAndSaveResetPasswordTokenForEmail(String email) {

    }

    @Override
    public Optional<String> getResetPasswordTokenByEmail(String email) {
        return Optional.empty();
    }
}
