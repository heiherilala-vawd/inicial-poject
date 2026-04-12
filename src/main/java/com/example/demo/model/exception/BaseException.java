package com.example.demo.model.exception;

import lombok.Getter;

public class BaseException extends RuntimeException {

  @Getter private final ExceptionType type;

  public BaseException(ExceptionType type, String message) {
    super(message);
    this.type = type;
  }

  public BaseException(ExceptionType type, Exception source) {
    super(source);
    this.type = type;
  }

  public enum ExceptionType {
    CLIENT_EXCEPTION,
    SERVER_EXCEPTION
  }
}
