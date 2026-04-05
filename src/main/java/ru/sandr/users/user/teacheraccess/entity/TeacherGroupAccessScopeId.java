package ru.sandr.users.user.teacheraccess.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class TeacherGroupAccessScopeId implements Serializable {

    @Column(name = "teacher_id", columnDefinition = "uuid")
    private UUID teacherId;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope_type", nullable = false, length = 20)
    private TeacherGroupAccessScopeType scopeType;

    @Column(name = "scope_id", nullable = false)
    private Long scopeId;
}
