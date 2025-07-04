package com.example.notificationservice.service;

import com.example.notificationservice.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.example.dtos.NotificationDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    public List<NotificationDto> getAllNotifications(Long userId) {
        Pageable pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "createdAt"));
        return notificationRepository.findByUserId(userId, pageable).stream()
                .map(item -> new NotificationDto(
                        item.getId(),
                        item.getUserId(),
                        item.getTitle(),
                        item.getMessage(),
                        item.getCreatedAt(),
                        item.isRead()
                ))
                .toList();
    }
}
