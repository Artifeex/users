package ru.sandr.users.security.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class HashUtils {
    public static String hashToken(String token) {
        try {
            // Используем именно SHA-256(потому что он очень быстрый и работает сильно быстрее BCrypt, который намерено замедлен)
            // Получили реализацию хэширование sha-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // Алгоритм sha-256 при выполнении хэширования работает не с символами, а с байтами
            // Поэтому мы должны сначала перевести нашу строку с токеном в байты, причем
            // указывать кодировку важно, т.к. если вдруг дефолтная кодировка сервера поменяется
            // То хэши одной и той же строки перестанут совпадать, т.к. при переводе строки в набор
            // байт поменяется кодировка
            byte[] encodedHash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            // Т.к. в encodedHash - байты, а мы храним в БД строки, то переводим эти байты в 16-ти ричную систему
            // 1 байт - 2 символа. Sha-256 всегда возвращает 32 байта. Поэтому в БД у нас будут все строки длиной 64 байта
            return HexFormat.of().formatHex(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hash algorithm not found", e);
        }
    }
}
