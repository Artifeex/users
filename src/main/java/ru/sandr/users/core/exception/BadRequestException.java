package ru.sandr.users.core.exception;

public class BadRequestException extends CustomException {

    public BadRequestException(String code, String message) {
        super(code, message);
    }
}
