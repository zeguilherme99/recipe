package com.platform.recipe.domain.exceptions;

public class DataNotFoundException extends GenericException {

  public DataNotFoundException(ErrorCode errorCodes) {
    super(errorCodes);
  }
}
