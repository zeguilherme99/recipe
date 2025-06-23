package com.platform.recipe.domain.repositories;

import com.platform.recipe.domain.entities.Recipe;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomRecipeJpaRepository {

  Page<Recipe> searchWithFilters(
    Boolean vegetarian,
    List<String> includedIngredients,
    List<String> excludedIngredients,
    String instruction,
    Instant createdAfter,
    Instant createdBefore,
    Pageable pageable
  );
}
