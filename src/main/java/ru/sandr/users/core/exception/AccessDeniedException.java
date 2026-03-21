package ru.sandr.users.core.exception;

public class AccessDeniedException extends CustomException {

    public AccessDeniedException(String code, String message) {
        super(code, message);
    }
}
