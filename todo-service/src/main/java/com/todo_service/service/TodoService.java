package com.todo_service.service;

import com.todo_service.dto.CreateTodoRequestDto;
import com.todo_service.dto.TodoResponseDto;
import com.todo_service.dto.UpdateTodoRequestDto;
import com.todo_service.entity.Todo;
import com.todo_service.entity.TodoStatus;
import com.todo_service.exception.NotFoundException;
import com.todo_service.mapper.TodoMapper;
import com.todo_service.repository.TodoRepository;
import com.todo_service.security.JwtUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TodoService {

    private final TodoRepository todoRepository;
    private final TodoMapper todoMapper;

    /**
     * Create to-do
     * @param request - CreateTodoRequestDto Object to create a to-do
     * @return - TodoResponse
     * @author - Isaac Hagan
     */
    @Transactional
    public TodoResponseDto createTodo(CreateTodoRequestDto request) {

        //  extracted directly from JWT
        UUID userId = JwtUtils.getCurrentUserId();
        String username = JwtUtils.getCurrentUserName();

        log.info("Creating todo for userId={}, username={}", userId, username);

        Todo todo = todoMapper.toTodo(request, userId, username);

        Todo savedTodo = todoRepository.save(todo);

        return todoMapper.todoResponseDto(savedTodo);
    }

    /**
     * Get all To-do's for authenticated user
     * @return TodoResponse
     *  @author - Isaac Hagan
     */
    public List<TodoResponseDto> getAllTodos() {

        UUID userId = JwtUtils.getCurrentUserId();

        return todoRepository.findAllByUserId(userId)
                .stream()
                .map(todoMapper::todoResponseDto)
                .toList();
    }

    /**
     *  Get To-do by ID
     * @param id - id of the user
     * @return TodoResponse
     *  @author - Isaac Hagan
     */
    public TodoResponseDto getTodo(UUID id) {

        UUID userId = JwtUtils.getCurrentUserId();

        Todo todo = todoRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException("Todo not found"));

        return todoMapper.todoResponseDto(todo);
    }


    /**
     * Update To-do By id
     * @param id - id of the user
     * @param request - object to be passed
     * @return TodoResponse
     * @author - Isaac Hagan
     */
    @Transactional
    public TodoResponseDto updateTodo(UUID id, UpdateTodoRequestDto request) {

        UUID userId = JwtUtils.getCurrentUserId();

        Todo todo = todoRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException("Todo not found"));

        if (request.getTitle() != null) {
            todo.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            todo.setDescription(request.getDescription());
        }

        if (request.getCategory() != null) {
            todo.setCategory(request.getCategory());
        }

        if (request.getPriority() != null) {
            todo.setPriority(request.getPriority());
        }

        if (request.getDueDate() != null) {
            todo.setDueDate(request.getDueDate());
        }

        if (request.getStatus() == TodoStatus.DONE) {
            todo.setStatus(TodoStatus.DONE);
            todo.setCompletedAt(LocalDateTime.now());
        }

        return todoMapper.todoResponseDto(todoRepository.save(todo));
    }


    /**
     * Soft delete To-do
     * @param id - the id of the user
     * @return TodoResponseDto
     * @author - Isaac Hagan
     */
    @Transactional
    public TodoResponseDto softDelete(UUID id) {

        UUID userId = JwtUtils.getCurrentUserId();

        Todo todo = todoRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException("Todo not found"));

        todo.setDeleted(true);
        todo.setDeletedAt(LocalDateTime.now());

        return todoMapper.todoResponseDto(todoRepository.save(todo));
    }
}
