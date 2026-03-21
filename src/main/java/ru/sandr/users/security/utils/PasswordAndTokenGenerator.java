package ru.sandr.users.security.utils;

import java.security.SecureRandom;

public class PasswordAndTokenGenerator {

    // Допустимые символы
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    // Исключаем похожие символы (например, l и 1, O и 0), чтобы юзеру было удобнее
    private static final String ALL_CHARS = LOWER + UPPER + DIGITS;

    // SecureRandom потокобезопасен (Thread-Safe),
    // поэтому мы можем создать один инстанс на все приложение
    private static final SecureRandom secureRandom = new SecureRandom();

    public static String generate(int length) {
        if (length < 8) {
            throw new IllegalArgumentException("Пароль должен быть не менее 8 символов");
        }

        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            // secureRandom.nextInt(bound) возвращает криптографически стойкое
            // случайное число от 0 до (bound - 1)
            int randomIndex = secureRandom.nextInt(ALL_CHARS.length());
            password.append(ALL_CHARS.charAt(randomIndex));
        }

        return password.toString();
    }
}