package com.platform.recipe.domain.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IngredientDto {

  private Long id;
  private String name;
  private int quantity;
  private String unit;
}
