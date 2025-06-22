package com.platform.recipe.adapters.controllers.config;

import com.platform.recipe.domain.exceptions.ErrorCode;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ResponseError implements Serializable {

  public static ResponseErrorMessage build(ErrorCode errorCode) {
    return build(errorCode, new ArrayList<>());
  }

  public static ResponseErrorMessage build(ErrorCode errorCode, List<FieldError> fieldErrors) {
    return new ResponseErrorMessage(errorCode.getCode(), errorCode.getTitle(), errorCode.getMessage(), fieldErrors);
  }

  public record ResponseErrorMessage(
      Integer code,
      String title,
      String message,
      List<FieldError> fieldErrors) {
  }

  public record FieldError(
      String name,
      List<String> details) {
  }
}
