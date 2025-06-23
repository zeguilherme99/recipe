package com.platform.recipe.domain.services.implementations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.recipe.domain.dtos.RecipeDto;
import com.platform.recipe.domain.entities.Ingredient;
import com.platform.recipe.domain.entities.Recipe;
import com.platform.recipe.domain.exceptions.DataNotFoundException;
import com.platform.recipe.domain.exceptions.ErrorCode;
import com.platform.recipe.domain.repositories.IngredientJpaRepository;
import com.platform.recipe.domain.repositories.RecipeJpaRepository;
import com.platform.recipe.domain.services.RecipeService;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Slf4j
@Service("recipeServiceImp")
@RequiredArgsConstructor
public class RecipeServiceImp implements RecipeService {

  private final RecipeJpaRepository recipeJpaRepository;
  private final IngredientJpaRepository ingredientJpaRepository;
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

  @Override
  public Page<RecipeDto> searchWithFilters(
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
  ) {

    PageRequest pageRequest = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());

    Page<Recipe> recipePage = recipeJpaRepository.searchWithFilters(
      vegetarian,
      includedIngredients,
      excludedIngredients,
      instruction,
      createdAfter,
      createdBefore,
      pageRequest
    );

    return convertPage(recipePage, serving);
  }

  private Page<RecipeDto> convertPage(Page<Recipe> recipePage, int serving) {
    List<Long> recipeIds = recipePage.getContent().stream().map(Recipe::getId).toList();
    List<Ingredient> ingredients = ingredientJpaRepository.findByRecipeIdIn(recipeIds);
    Map<Long, List<Ingredient>> grouped = ingredients.stream()
      .collect(Collectors.groupingBy(i -> i.getRecipe().getId()));

    recipePage.getContent().forEach(recipe -> {
      recipe.setIngredients(grouped.getOrDefault(recipe.getId(), List.of()));
    });

    return recipePage.map(recipe -> {
      if (serving > 1) {
        recipe.getIngredients().forEach(
          ingredient -> ingredient.setQuantity(ingredient.getQuantity() * serving)
        );
      }

      return objectMapper.convertValue(recipe, RecipeDto.class);
    });
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
