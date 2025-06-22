package com.platform.recipe.domain.dtos;

import java.sql.Timestamp;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
