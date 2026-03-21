package ru.sandr.users.core.exception;

public class UnauthorizedException extends CustomException {
    public UnauthorizedException(String code, String message) {
        super(code, message);
    }
}

