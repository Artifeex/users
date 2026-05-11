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
@Table(name = "reset_password_token", schema = "auth")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "token_hash", unique = true, nullable = false)
    private String tokenHash;

    @NotNull(message = "user is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @NotNull(message = "expiryAt is required")
    @Column(name = "expiry_at", nullable = false)
    private Instant expiryAt;

    @NotNull(message = "createdAt is required")
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
