package com.platform.recipe.adapters.controllers.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IngredientRequest {

  private Long id;

  @NotBlank(message = "Name is required.")
  @Size(min = 3, max = 255, message = "Name size must be between 3 and 255")
  private String name;

  @Positive(message = "Quantity must be bigger than zero")
  private int quantity;
  private String unit;
}
