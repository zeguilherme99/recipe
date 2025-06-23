package com.platform.recipe.domain.services;

import com.platform.recipe.domain.dtos.RecipeDto;
import com.platform.recipe.domain.exceptions.DataNotFoundException;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;

public interface RecipeService {

  Long create(RecipeDto recipe);
  RecipeDto update(RecipeDto recipe) throws DataNotFoundException;
  void deleteById(Long id) throws DataNotFoundException;
  RecipeDto findById(Long id) throws DataNotFoundException;
  Page<RecipeDto> searchWithFilters(
    Boolean vegetarian,
    int serving,
    List<String> includedIngredients,
    List<String> excludedIngredients,
    String instruction,
    Instant createdAfter,
    Instant createdBefore,
    int page,
    int pageSize,
    String sort
  );
}
