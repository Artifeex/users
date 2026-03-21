package ru.sandr.users.hierarchy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sandr.users.hierarchy.entity.FieldOfStudy;

public interface FieldOfStudyRepository extends JpaRepository<FieldOfStudy, Long> {

    boolean existsByFaculty_Id(Long facultyId);
}
