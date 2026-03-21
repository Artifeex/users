package ru.sandr.users.security.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.sandr.users.user.entity.User;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "refresh_token", schema = "auth")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull // тогда если мы попытаемся отправить сохранение/обновление такого объекта, то сам hibernate проверит пустоту
    // и если окажется, что здесь null, то выбросить ConstraintViolationException еще до того, как
    // сделате запрос в БД. Т.е. fail-fast
    @Column(name = "token_hash", unique = true, nullable = false) // UNIQUE соответствует liquibase (auth.refresh_token.token_hash)
    private String tokenHash;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @NotNull
    @Column(name = "expiry_at", nullable = false)
    private Instant expiryAt;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
