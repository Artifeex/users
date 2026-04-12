package ru.sandr.users.teacheraccess.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.sandr.users.teacheraccess.entity.TeacherGroupAccessScope;
import ru.sandr.users.teacheraccess.entity.TeacherGroupAccessScopeId;
import ru.sandr.users.teacheraccess.entity.TeacherGroupAccessScopeType;

import java.util.List;
import java.util.UUID;

public interface TeacherGroupAccessScopeRepository extends JpaRepository<TeacherGroupAccessScope, TeacherGroupAccessScopeId> {

    List<TeacherGroupAccessScope> findAllByTeacher_Id(UUID teacherId);

    Page<TeacherGroupAccessScope> findAllByTeacher_IdAndId_ScopeType(
            UUID teacherId,
            TeacherGroupAccessScopeType scopeType,
            Pageable pageable
    );

    void deleteByTeacher_Id(UUID teacherId);
}
