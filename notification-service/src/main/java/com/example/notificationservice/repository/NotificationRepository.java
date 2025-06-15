package com.example.notificationservice.repository;

import com.example.notificationservice.entity.Notification;
import org.example.dtos.NotificationDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserId(Long userId, Pageable pageable);
}
