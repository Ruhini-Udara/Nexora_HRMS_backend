package com.hexaco.hrms.service;

import com.hexaco.hrms.dto.NotificationDto;
import com.hexaco.hrms.models.Employee;
import com.hexaco.hrms.models.Notification;
import com.hexaco.hrms.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SystemNotificationService {

    private final NotificationRepository notificationRepository;

    public List<NotificationDto> getUnreadNotifications(Long employeeId) {
        return notificationRepository.findByEmployeeIdAndIsReadFalseOrderByCreatedAtDesc(employeeId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<NotificationDto> getAllNotifications(Long employeeId) {
        return notificationRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    public long getUnreadCount(Long employeeId) {
        return notificationRepository.countByEmployeeIdAndIsReadFalse(employeeId);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void createNotification(Employee employee, String title, String message, String link) {
        Notification notification = Notification.builder()
                .employee(employee)
                .title(title)
                .message(message)
                .link(link)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }

    private NotificationDto mapToDto(Notification notification) {
        return NotificationDto.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .isRead(notification.isRead())
                .link(notification.getLink())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
