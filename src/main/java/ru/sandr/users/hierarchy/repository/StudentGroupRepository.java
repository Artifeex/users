package ru.sandr.users.hierarchy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sandr.users.hierarchy.entity.StudentGroup;

public interface StudentGroupRepository extends JpaRepository<StudentGroup, Long> {

    boolean existsByFaculty_Id(Long facultyId);

    boolean existsByFieldOfStudy_Id(Long fieldOfStudyId);
}

