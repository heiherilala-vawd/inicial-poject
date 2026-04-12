package com.example.demo.model.exception;

public class NotImplementedException extends BaseException {
  public NotImplementedException(String message) {
    super(ExceptionType.SERVER_EXCEPTION, message);
  }
}
