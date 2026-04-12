package com.example.demo.model.exception;

public class BadRequestException extends BaseException {
  public BadRequestException(String message) {
    super(ExceptionType.CLIENT_EXCEPTION, message);
  }
}
