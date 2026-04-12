package com.example.demo.model;

import lombok.Getter;

public class PageFromOne {

  @Getter private final int value;

  public PageFromOne(String value) {
    int intValue = Integer.parseInt(value);
    if (intValue < 1) {
      // throw new BadRequestException("page value must be >=1");
    }
    this.value = intValue;
  }
}
