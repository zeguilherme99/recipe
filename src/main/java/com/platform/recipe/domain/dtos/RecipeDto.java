package com.platform.recipe.domain.dtos;

import java.sql.Timestamp;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecipeDto {

  private Long id;
  private String title;
  private String description;
  private boolean vegetarian;
  private String instructions;
  private List<IngredientDto> ingredients;
  private Timestamp createdAt;
  private Timestamp updatedAt;
}
