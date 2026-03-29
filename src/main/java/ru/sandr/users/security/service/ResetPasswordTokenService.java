package ru.sandr.users.security.service;

import ru.sandr.users.security.dto.ForgotPasswordRequestDto;
import ru.sandr.users.security.dto.ResetPasswordRequestDto;
import ru.sandr.users.user.entity.User;

import java.util.Optional;

public interface ResetPasswordTokenService {

    void generateAndSaveResetPasswordTokenForUser(User user);

    void resetPassword(ResetPasswordRequestDto requestDto);

    void forgotPassword(ForgotPasswordRequestDto forgotPasswordRequestDto);
}
