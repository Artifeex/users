package ru.sandr.users.hierarchy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.sandr.users.hierarchy.entity.FieldOfStudy;

import java.util.Collection;
import java.util.List;

public interface FieldOfStudyRepository extends JpaRepository<FieldOfStudy, Long> {

    boolean existsByFaculty_Id(Long facultyId);
}
