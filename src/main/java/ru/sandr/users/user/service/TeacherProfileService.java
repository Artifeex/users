package ru.sandr.users.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sandr.users.user.repository.TeacherProfileRepository;

@Service
@RequiredArgsConstructor
public class TeacherProfileService {

    private final TeacherProfileRepository teacherProfileRepository;

    @Transactional(readOnly = true)
    public boolean hasTeachersInDepartment(Long departmentId) {
        return teacherProfileRepository.existsByDepartment_Id(departmentId);
    }
}
