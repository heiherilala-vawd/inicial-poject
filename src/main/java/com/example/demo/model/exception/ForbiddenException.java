package com.example.demo.model.exception;

public class ForbiddenException extends BaseException {

  public ForbiddenException() {
    super(ExceptionType.CLIENT_EXCEPTION, "Access is denied");
  }

  public ForbiddenException(String message) {
    super(ExceptionType.CLIENT_EXCEPTION, message);
  }
}
