package com.platform.recipe.domain.services;

import com.platform.recipe.domain.dtos.RecipeDto;

public interface RecipeService {

  Long create(RecipeDto recipe);
}
