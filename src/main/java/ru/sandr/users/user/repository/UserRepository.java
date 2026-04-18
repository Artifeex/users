package ru.sandr.users.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.sandr.users.user.entity.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    Optional<User> findByUsernameOrEmail(String username, String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    /**
     * Returns only the usernames that already exist in the DB from the given set.
     * Used for per-batch duplicate detection during bulk import — avoids loading all 500K+ usernames.
     */
    @Query("SELECT u.username FROM User u WHERE u.username IN :usernames")
    List<String> findExistingUsernamesIn(@Param("usernames") Collection<String> usernames);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.studentProfile WHERE u.username IN :usernames")
    List<User> findAllByUsernameInWithStudentProfile(@Param("usernames") Collection<String> usernames);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.teacherProfile WHERE u.username IN :usernames")
    List<User> findAllByUsernameInWithTeacherProfile(@Param("usernames") Collection<String> usernames);

    @Query("SELECT u.email AS email, u.username AS username FROM User u WHERE u.email IN :emails AND u.email IS NOT NULL")
    List<EmailUsernameProjection> findAllByEmailIn(@Param("emails") Collection<String> emails);

    @EntityGraph(attributePaths = {
            "userRoles",
            "userRoles.role",
            "teacherProfile",
            "teacherProfile.department",
            "studentProfile",
            "studentProfile.department"
    })
    @Query("SELECT DISTINCT u FROM User u WHERE u.id IN :ids")
    List<User> findAllByIdInWithRoles(@Param("ids") Collection<UUID> ids);

    @EntityGraph(attributePaths = {
            "userRoles",
            "userRoles.role",
            "teacherProfile",
            "teacherProfile.department",
            "teacherProfile.department.faculty",
            "studentProfile",
            "studentProfile.department",
            "studentProfile.department.faculty",
            "studentProfile.group",
            "studentProfile.group.faculty",
            "studentProfile.group.fieldOfStudy"
    })
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdWithDetails(@Param("id") UUID id);

    Optional<User> findByUsername(String username);

    interface EmailUsernameProjection {
        String getEmail();

        String getUsername();
    }
}
