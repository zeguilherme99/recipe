package com.platform.recipe.domain.services;

import com.platform.recipe.domain.dtos.RecipeDto;
import com.platform.recipe.domain.exceptions.DataNotFoundException;

public interface RecipeService {

  Long create(RecipeDto recipe);
  RecipeDto update(RecipeDto recipe) throws DataNotFoundException;
}
