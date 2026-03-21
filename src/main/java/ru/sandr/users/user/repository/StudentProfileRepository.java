package ru.sandr.users.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sandr.users.user.entity.StudentProfile;

import java.util.UUID;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, UUID> {

    boolean existsByGroup_Id(Long groupId);
}

