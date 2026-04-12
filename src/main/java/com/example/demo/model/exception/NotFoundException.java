package com.example.demo.model.exception;

public class NotFoundException extends BaseException {
  public NotFoundException(String message) {
    super(ExceptionType.CLIENT_EXCEPTION, message);
  }
}
