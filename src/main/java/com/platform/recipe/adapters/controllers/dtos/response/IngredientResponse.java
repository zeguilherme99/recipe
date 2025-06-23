package com.platform.recipe.adapters.controllers.dtos.response;

import lombok.Value;

@Value
public class IngredientResponse {

  Long id;
  String name;
  int quantity;
  String unit;
}
