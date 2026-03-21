package ru.sandr.users.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sandr.users.user.repository.StudentProfileRepository;

@Service
@RequiredArgsConstructor
public class StudentProfileService {

    private final StudentProfileRepository studentProfileRepository;

    @Transactional(readOnly = true)
    public boolean hasStudentsInGroup(Long groupId) {
        return studentProfileRepository.existsByGroup_Id(groupId);
    }
}
