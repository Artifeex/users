package ru.sandr.users.teacheraccess.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sandr.users.teacheraccess.entity.TeacherGroupAccessScope;
import ru.sandr.users.teacheraccess.entity.TeacherGroupAccessScopeId;

import java.util.List;
import java.util.UUID;

public interface TeacherGroupAccessScopeRepository extends JpaRepository<TeacherGroupAccessScope, TeacherGroupAccessScopeId> {

    List<TeacherGroupAccessScope> findAllByTeacher_Id(UUID teacherId);

    void deleteByTeacher_Id(UUID teacherId);
}
