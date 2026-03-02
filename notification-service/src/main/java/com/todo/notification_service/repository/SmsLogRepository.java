package com.todo.notification_service.repository;

import com.todo.notification_service.entity.SmsLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SmsLogRepository extends JpaRepository<SmsLog, UUID> {

}
