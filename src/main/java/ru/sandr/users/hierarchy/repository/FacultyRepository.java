package ru.sandr.users.hierarchy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sandr.users.hierarchy.entity.Faculty;

public interface FacultyRepository extends JpaRepository<Faculty, Long> {
}

