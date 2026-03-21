package ru.sandr.users;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class UsersApplication {

    @PostConstruct
    public void init() {
        // Устанавливаем часовой пояс приложения в UTC
        TimeZone.setDefault(TimeZone.getTimeZone("UTC")); // Вся система(включая LocalDateTime будет работать по UTC)
        // По дефолту используется TimeZone нашей системы. Т.е. вот сейчас у меня на компьютере
        // Стоит UTC+3, поэтому в LocalDateTime будет записано мое текущее время. Т.е. например, сейчас
        // по UTC+3: 9:00. То в LocalDateTime запишется 9:00.
        // Но, если сервер, на котором будет работать наше java приложение переедет или
        // просто в нем кто-то поменяет часовой пояс, то начнутся проблемы, т.е. UTC+3 может превратиться в
        // UTC+5
        // Если же мы ставим UTC, то время всегда хранится в UTC+0, где бы не находился наш сервер
        // Так же полезно для логов, т.к. если у нас в БД время по UTC, то и в логах мы бы хотели
        // видеть те же числа, что и в created_at полях в БД. А логеры обычно используют локально время
        // при логировании и тогда придется каждый раз помнить об этом и прибавлять или вычитать часы
        // от локального времени
    }

    public static void main(String[] args) {
        SpringApplication.run(UsersApplication.class, args);
    }

}
