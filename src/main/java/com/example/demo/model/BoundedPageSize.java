package com.example.demo.model;

import lombok.Getter;

public class BoundedPageSize {

  @Getter private final int value;

  private static final int MAX_SIZE = 500;

  public BoundedPageSize(String value) {
    int intValue = Integer.parseInt(value);
    if (intValue < 1) {
      // throw new BadRequestException("page value must be >=1");
    }
    if (intValue > MAX_SIZE) {
      // throw new BadRequestException("page size must be <" + MAX_SIZE);
    }
    this.value = intValue;
  }
}
