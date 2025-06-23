package com.platform.recipe.adapters.controllers.dtos.response;

import java.sql.Timestamp;
import java.util.List;
import lombok.Value;

@Value
public class RecipeResponse {

  Long id;
  String title;
  String description;
  boolean vegetarian;
  String instructions;
  List<IngredientResponse> ingredients;
  Timestamp createdAt;
  Timestamp updatedAt;
}
