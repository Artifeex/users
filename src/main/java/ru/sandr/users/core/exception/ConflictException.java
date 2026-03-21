package ru.sandr.users.core.exception;

public class ConflictException extends CustomException {

    public ConflictException(String code, String message) {
        super(code, message);
    }
}
