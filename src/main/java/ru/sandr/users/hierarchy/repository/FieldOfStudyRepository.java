package ru.sandr.users.hierarchy.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.sandr.users.hierarchy.entity.FieldOfStudy;

import java.util.Collection;
import java.util.List;

public interface FieldOfStudyRepository extends JpaRepository<FieldOfStudy, Long> {

    boolean existsByFaculty_Id(Long facultyId);

    List<FieldOfStudy> findAllByNameIn(Collection<String> names);

    Page<FieldOfStudy> findAllByFaculty_Id(Long facultyId, Pageable pageable);
}
