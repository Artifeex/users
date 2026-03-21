package ru.sandr.users.core.exception;

public class ObjectNotFoundException extends CustomException {

    public ObjectNotFoundException(String code, String message) {
        super(code, message);
    }
}
