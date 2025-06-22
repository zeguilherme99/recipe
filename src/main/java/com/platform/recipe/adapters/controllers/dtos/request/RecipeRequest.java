package com.platform.recipe.adapters.controllers.dtos.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RecipeRequest {

  @NotBlank(message = "Title is required.")
  @Size(min = 3, max = 255, message = "Title size must be between 3 and 255")
  private String title;

  @Size(min = 3, max = 255, message ="Description size must be between 3 and 255")
  private String description;

  private boolean vegetarian;

  @NotBlank(message = "Instructions are required.")
  private String instructions;

  @NotNull(message = "The ingredients list is required.")
  @Size(min = 1, message = "The recipe must contain 1 ingredient at least.")
  @Valid
  private List<IngredientRequest> ingredients;
}
