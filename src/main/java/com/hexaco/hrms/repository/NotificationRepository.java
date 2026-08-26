package com.hexaco.hrms.repository;

import com.hexaco.hrms.models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);

    List<Notification> findByEmployeeIdAndIsReadFalseOrderByCreatedAtDesc(Long employeeId);

    long countByEmployeeIdAndIsReadFalse(Long employeeId);
}
