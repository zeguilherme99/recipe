package com.platform.recipe.domain.exceptions;

public class UnexpectedErrorException extends GenericException {

  public UnexpectedErrorException(ErrorCode errorCodes, Throwable throwable) {
    super(errorCodes, throwable);
  }
}
