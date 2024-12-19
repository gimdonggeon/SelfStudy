package com.example.demo.service;

import com.example.demo.dto.ReportResponseDto;
import com.example.demo.entity.User;
import com.example.demo.exception.ReportUsersException;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {
    private final UserRepository userRepository;

    public AdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // TODO: 4. find or save 예제 개선
    @Transactional
    public ReportResponseDto reportUsers(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            throw new IllegalArgumentException("사용자 ID 목록이 비어있습니다.");
        }

        int updatedCount = userRepository.updateStatusToBlockedForUsers(userIds);

        return new ReportResponseDto(updatedCount, userIds);
    }
}
