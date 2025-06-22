package com.platform.recipe.domain.services.implementations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.recipe.domain.dtos.RecipeDto;
import com.platform.recipe.domain.entities.Recipe;
import com.platform.recipe.domain.exceptions.DataNotFoundException;
import com.platform.recipe.domain.exceptions.ErrorCode;
import com.platform.recipe.domain.repositories.RecipeJpaRepository;
import com.platform.recipe.domain.services.RecipeService;
import java.sql.Timestamp;
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
    Recipe saved = persistRecipe(recipe);
    return saved.getId();
  }

  @Override
  public RecipeDto update(RecipeDto recipe) throws DataNotFoundException {
    log.info("Preparing to update recipe id [{}]", recipe.getId());

    Recipe entity = recipeJpaRepository.findById(recipe.getId())
        .orElseThrow(() -> new DataNotFoundException(ErrorCode.RECIPE_NOT_FOUND));

    Timestamp createdAt = entity.getCreatedAt();
    Recipe saved = persistRecipe(recipe);
    RecipeDto recipeDto = objectMapper.convertValue(saved, RecipeDto.class);
    recipeDto.setCreatedAt(createdAt);
    return recipeDto;
  }

  private Recipe persistRecipe(RecipeDto recipe) {
    Recipe entity = objectMapper.convertValue(recipe, Recipe.class);
    entity.getIngredients().forEach(i -> i.setRecipe(entity));

    return recipeJpaRepository.save(entity);
  }

}
