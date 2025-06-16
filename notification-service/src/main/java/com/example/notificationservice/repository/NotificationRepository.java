package com.example.notificationservice.repository;

import com.example.notificationservice.entity.Notification;
import org.example.dtos.NotificationDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("select n from Notification n where n.userId = :userId and n.isRead = false ")
    List<Notification> findByUserId(Long userId, Pageable pageable);

    @Modifying
    @Query("update Notification n set n.isRead = true where n.id = :id")
    void updateStatusNotify(Long id);
}
