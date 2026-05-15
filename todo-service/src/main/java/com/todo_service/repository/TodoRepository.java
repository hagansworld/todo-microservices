package com.todo_service.repository;

import com.todo_service.entity.Todo;
import com.todo_service.entity.TodoCategory;
import com.todo_service.entity.TodoPriority;
import com.todo_service.entity.TodoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TodoRepository extends JpaRepository<Todo, UUID> {

    // used by getTodo — excludes soft-deleted
    Optional<Todo> findByIdAndUserIdAndIsDeletedFalse(UUID id, UUID userId);

    // trash — only deleted items
    List<Todo> findAllByUserIdAndIsDeletedTrue(UUID userId);

    // Get all todos created by a specific user
    List<Todo> findAllByUserId(UUID userId);

    // find upcoming todos between now and a specific time window
    List<Todo> findByStatusInAndDueDateBetween(
            List<TodoStatus> statuses,
            LocalDateTime start,
            LocalDateTime end
    );

    List<Todo> findByStatusInAndDueDateBefore(List<TodoStatus> statuses, LocalDateTime time);
    /**
     * Flexible search — all filters are optional.
     * Passing null for any param means "ignore that filter".
     */
    @Query("""
        SELECT t FROM Todo t
        WHERE t.userId = :userId
          AND t.isDeleted = false
          AND (:keyword   IS NULL OR LOWER(t.title)       LIKE LOWER(CONCAT('%', :keyword, '%'))
                                  OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:category  IS NULL OR t.category  = :category)
          AND (:priority  IS NULL OR t.priority  = :priority)
          AND (:status    IS NULL OR t.status    = :status)
          AND (:dueBefore IS NULL OR t.dueDate  <= :dueBefore)
          AND (:dueAfter  IS NULL OR t.dueDate  >= :dueAfter)
        ORDER BY
          CASE t.priority WHEN 'HIGH' THEN 1 WHEN 'MEDIUM' THEN 2 ELSE 3 END,
          t.dueDate ASC NULLS LAST
    """)
    List<Todo> search(
            @Param("userId")    UUID userId,
            @Param("keyword")   String keyword,
            @Param("category") TodoCategory category,
            @Param("priority") TodoPriority priority,
            @Param("status")    TodoStatus status,
            @Param("dueBefore") LocalDateTime dueBefore,
            @Param("dueAfter")  LocalDateTime dueAfter
    );

    // permanent delete — only allowed if already soft-deleted
    @Modifying
    @Query("DELETE FROM Todo t WHERE t.id = :id AND t.userId = :userId AND t.isDeleted = true")
    void permanentDelete(@Param("id") UUID id, @Param("userId") UUID userId);


    Optional<Todo> findByIdAndUserIdAndIsDeletedTrue(UUID id, UUID userId);
}
