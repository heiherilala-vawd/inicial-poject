package com.example.demo.model.exception;

public class TooManyRequestsException extends BaseException {
  public TooManyRequestsException(String message) {
    super(ExceptionType.CLIENT_EXCEPTION, message);
  }

  public TooManyRequestsException(Exception source) {
    super(ExceptionType.CLIENT_EXCEPTION, source);
  }
}
