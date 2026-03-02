package com.todo.notification_service.exception;

public class EmailFailedException extends RuntimeException {
  public EmailFailedException(String message) {
    super(message);
  }
}
