package com.platform.recipe.domain.exceptions;

import lombok.Getter;

@Getter
public class GenericException extends Exception {

  private final ErrorCode errorCode;

  public GenericException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

  public GenericException(ErrorCode errorCode, Throwable throwable) {
    super(errorCode.getMessage(), throwable);
    this.errorCode = errorCode;
  }
}
