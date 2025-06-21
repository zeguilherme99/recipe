package com.platform.recipe.domain.services.implementations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.recipe.domain.dtos.IngredientDto;
import com.platform.recipe.domain.dtos.RecipeDto;
import com.platform.recipe.domain.entities.Ingredient;
import com.platform.recipe.domain.entities.Recipe;
import com.platform.recipe.domain.repositories.RecipeJpaRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecipeServiceImpTest {

  @InjectMocks
  private RecipeServiceImp recipeService;

  @Mock
  private RecipeJpaRepository recipeJpaRepository;

  @Mock
  private ObjectMapper objectMapper;

  @Test
  void shouldCreateRecipeAndReturnId() {

    RecipeDto recipeDto = new RecipeDto();
    recipeDto.setTitle("Feijoada");
    recipeDto.setDescription("Brazilian food");
    recipeDto.setVegetarian(false);
    recipeDto.setInstructions("Cook for 2h");

    IngredientDto firstIngredientDto = new IngredientDto(null, "bean", 500, "g");
    IngredientDto secondIngredientDto = new IngredientDto(null, "brad", 300, "g");

    recipeDto.setIngredients(List.of(firstIngredientDto, secondIngredientDto));

    Recipe entity = new Recipe();
    entity.setTitle(recipeDto.getTitle());
    entity.setDescription(recipeDto.getDescription());
    entity.setVegetarian(recipeDto.isVegetarian());
    entity.setInstructions(recipeDto.getInstructions());

    Ingredient firstIngredient = new Ingredient();
    firstIngredient.setName(firstIngredientDto.getName());
    firstIngredient.setQuantity(firstIngredientDto.getQuantity());
    firstIngredient.setUnit(firstIngredientDto.getUnit());

    Ingredient secondIngredient = new Ingredient();
    secondIngredient.setName(secondIngredientDto.getName());
    secondIngredient.setQuantity(secondIngredientDto.getQuantity());
    secondIngredient.setUnit(secondIngredientDto.getUnit());

    entity.setIngredients(List.of(firstIngredient, secondIngredient));

    Recipe saved = new Recipe();
    saved.setId(1L);

    firstIngredient.setRecipe(entity);
    secondIngredient.setRecipe(entity);

    when(objectMapper.convertValue(recipeDto, Recipe.class)).thenReturn(entity);
    when(recipeJpaRepository.save(entity)).thenReturn(saved);

    Long returnedId = recipeService.create(recipeDto);

    assertEquals(1L, returnedId);
    verify(recipeJpaRepository).save(entity);
    verify(objectMapper).convertValue(recipeDto, Recipe.class);
  }
}