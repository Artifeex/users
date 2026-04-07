package ru.sandr.users.teacheraccess.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sandr.users.core.entity.AuditableEntity;
import ru.sandr.users.user.entity.TeacherProfile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "teacher_group_access_scopes", schema = "users")
public class TeacherGroupAccessScope extends AuditableEntity {

    @EmbeddedId
    private TeacherGroupAccessScopeId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("teacherId") // Из EmbeddedId нужно взять поле teacherId и тогда будет связь на teacher
    @JoinColumn(name = "teacher_id", nullable = false)
    private TeacherProfile teacher;
}
