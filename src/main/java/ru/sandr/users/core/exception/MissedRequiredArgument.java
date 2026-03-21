package ru.sandr.users.core.exception;

public class MissedRequiredArgument extends CustomException {
    public MissedRequiredArgument(String code, String message) {
        super(code,  message);
    }
}
