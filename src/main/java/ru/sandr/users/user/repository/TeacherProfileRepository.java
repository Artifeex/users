package ru.sandr.users.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sandr.users.user.entity.TeacherProfile;

import java.util.Optional;
import java.util.UUID;

public interface TeacherProfileRepository extends JpaRepository<TeacherProfile, UUID> {

    boolean existsByDepartment_Id(Long departmentId);

    Optional<TeacherProfile> findByUser_UsernameOrUser_Email(String username, String email);

    Optional<TeacherProfile> findByUser_Id(UUID userId);
}

