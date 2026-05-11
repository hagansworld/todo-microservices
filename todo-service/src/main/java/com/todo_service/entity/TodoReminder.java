package com.todo_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(
        name = "tbl_todo_reminder",
        uniqueConstraints = @UniqueConstraint(columnNames = {"todo_id", "reminder_type"})
)
public class TodoReminder {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "todo_id", nullable = false)
    private UUID todoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reminder_type", nullable = false)
    private TodoReminderType reminderType;

    @CreationTimestamp
    @Column(name = "sent_at", nullable = false, updatable = false)
    private LocalDateTime sentAt;
}