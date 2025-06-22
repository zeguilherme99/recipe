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

    Recipe entity = findRecipeById(recipe.getId());

    Timestamp createdAt = entity.getCreatedAt();
    Recipe saved = persistRecipe(recipe);
    RecipeDto recipeDto = objectMapper.convertValue(saved, RecipeDto.class);
    recipeDto.setCreatedAt(createdAt);
    return recipeDto;
  }

  @Override
  public void deleteById(Long id) throws DataNotFoundException {
    log.info("Preparing to delete recipe id [{}]", id);
    recipeExistsById(id);
    recipeJpaRepository.deleteById(id);
  }

  @Override
  public RecipeDto findById(Long id) throws DataNotFoundException {
    log.info("Preparing to find recipe id [{}]", id);

    Recipe recipe = findRecipeById(id);

    return objectMapper.convertValue(recipe, RecipeDto.class);
  }

  private Recipe findRecipeById(Long id) throws DataNotFoundException {
    return recipeJpaRepository.findById(id).orElseThrow(() -> {
      log.info("Recipe not found id: [{}]", id);
      return new DataNotFoundException(ErrorCode.RECIPE_NOT_FOUND);
    });
  }

  private void recipeExistsById(Long id) throws DataNotFoundException {
    if (!recipeJpaRepository.existsById(id)) {
      log.info("Recipe not found id [{}]", id);
      throw new DataNotFoundException(ErrorCode.RECIPE_NOT_FOUND);
    }
  }

  private Recipe persistRecipe(RecipeDto recipe) {
    Recipe entity = objectMapper.convertValue(recipe, Recipe.class);
    entity.getIngredients().forEach(i -> i.setRecipe(entity));

    return recipeJpaRepository.save(entity);
  }

}
