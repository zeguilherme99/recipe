package com.platform.recipe.domain.services.implementations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.recipe.domain.dtos.RecipeDto;
import com.platform.recipe.domain.entities.Recipe;
import com.platform.recipe.domain.repositories.RecipeJpaRepository;
import com.platform.recipe.domain.services.RecipeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service("recipeServiceImp")
@RequiredArgsConstructor
public class RecipeServiceImp implements RecipeService {

  private final RecipeJpaRepository recipeJpaRepository;
  private final ObjectMapper objectMapper;

  @Override
  public Long create(RecipeDto recipe) {
    log.info("Preparing to save new Recipe [{}]", recipe.getTitle());

    Recipe entity = objectMapper.convertValue(recipe, Recipe.class);
    entity.getIngredients().forEach(i -> i.setRecipe(entity));

    Recipe saved = recipeJpaRepository.save(entity);
    return saved.getId();
  }
}
