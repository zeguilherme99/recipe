package com.platform.recipe.adapters.controllers.config;

import com.platform.recipe.domain.exceptions.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Schema(name = "ResponseError", description = "Standard API Error Object")
public class ResponseError implements Serializable {

  public static ResponseErrorMessage build(ErrorCode errorCode) {
    return build(errorCode, new ArrayList<>());
  }

  public static ResponseErrorMessage build(ErrorCode errorCode, List<FieldError> fieldErrors) {
    return new ResponseErrorMessage(errorCode.getCode(), errorCode.getTitle(), errorCode.getMessage(), fieldErrors);
  }

  public record ResponseErrorMessage(
    @Schema(example = "100", description = "Error code")
    Integer code,

    @Schema(example = "Invalid data", description = "Error title")
    String title,

    @Schema(example = "Mandatory field not provided", description = "Detailed description")
    String message,

    @Schema(description = "List of errors by field")
    List<FieldError> fieldErrors
  ) {
  }

  @Schema(name = "FieldError", description = "Error details for a specific field")
  public record FieldError(
    @Schema(example = "email", description = "Invalid field name")
    String name,

    @Schema(description = "List of messages related to this field")
    List<String> details
  ) {
  }
}
