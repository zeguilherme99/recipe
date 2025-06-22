package com.platform.recipe.domain.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

  UNEXPECTED_ERROR  (100, "Unexpected Error", "An unexpected error has occurred, please try again."),
  INVALID_DATA      (101, "Invalid data", "The data provided is invalid for this operation."),
  RECIPE_NOT_FOUND  (102, "Data not found", "Recipe not found.");

  private final Integer code;
  private final String title;
  private final String message;
}
