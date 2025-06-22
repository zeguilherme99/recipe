package com.platform.recipe.domain.services.implementations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.recipe.domain.dtos.IngredientDto;
import com.platform.recipe.domain.dtos.RecipeDto;
import com.platform.recipe.domain.entities.Ingredient;
import com.platform.recipe.domain.entities.Recipe;
import com.platform.recipe.domain.exceptions.DataNotFoundException;
import com.platform.recipe.domain.repositories.RecipeJpaRepository;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
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

    RecipeDto recipeDto = createDto();
    Recipe entity = createRecipe(recipeDto);

    Recipe saved = new Recipe();
    saved.setId(1L);

    when(objectMapper.convertValue(recipeDto, Recipe.class)).thenReturn(entity);
    when(recipeJpaRepository.save(entity)).thenReturn(saved);

    Long returnedId = recipeService.create(recipeDto);

    assertEquals(1L, returnedId);
    verify(recipeJpaRepository).save(entity);
    verify(objectMapper).convertValue(recipeDto, Recipe.class);
  }

  @Test
  void shouldUpdateRecipeSuccessfully() throws DataNotFoundException {
    Long id = 1L;
    RecipeDto dto = new RecipeDto();
    dto.setId(id);
    dto.setTitle("Updated");
    dto.setIngredients(List.of());

    Recipe existing = new Recipe();
    existing.setId(id);
    existing.setCreatedAt(Timestamp.valueOf("2024-01-01 10:00:00"));

    Recipe updated = new Recipe();
    updated.setId(id);
    updated.setCreatedAt(existing.getCreatedAt());

    RecipeDto mappedResult = new RecipeDto();
    mappedResult.setId(id);
    mappedResult.setTitle("Updated");
    mappedResult.setCreatedAt(existing.getCreatedAt());

    when(recipeJpaRepository.findById(id)).thenReturn(Optional.of(existing));
    when(objectMapper.convertValue(dto, Recipe.class)).thenReturn(updated);
    when(recipeJpaRepository.save(any())).thenReturn(updated);
    when(objectMapper.convertValue(updated, RecipeDto.class)).thenReturn(mappedResult);

    RecipeDto result = recipeService.update(dto);

    assertNotNull(result);
    assertEquals(result.getId(), id);
    assertEquals(result.getCreatedAt(), existing.getCreatedAt());
    assertEquals(result.getTitle(), "Updated");

    verify(recipeJpaRepository).save(any(Recipe.class));
  }

  @Test
  void shouldThrowExceptionWhenRecipeNotFound() {
    Long id = 99L;
    RecipeDto dto = new RecipeDto();
    dto.setId(id);

    when(recipeJpaRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(DataNotFoundException.class, () -> recipeService.update(dto));
    verify(recipeJpaRepository, never()).save(any());
  }

  private RecipeDto createDto() {
    RecipeDto recipeDto = new RecipeDto();
    recipeDto.setTitle("Feijoada");
    recipeDto.setDescription("Brazilian food");
    recipeDto.setVegetarian(false);
    recipeDto.setInstructions("Cook for 2h");

    IngredientDto firstIngredientDto = new IngredientDto(null, "bean", 500, "g");
    IngredientDto secondIngredientDto = new IngredientDto(null, "brad", 300, "g");

    recipeDto.setIngredients(List.of(firstIngredientDto, secondIngredientDto));

    return recipeDto;
  }

  private Recipe createRecipe(RecipeDto recipeDto) {
    Recipe entity = new Recipe();
    entity.setTitle(recipeDto.getTitle());
    entity.setDescription(recipeDto.getDescription());
    entity.setVegetarian(recipeDto.isVegetarian());
    entity.setInstructions(recipeDto.getInstructions());

    Ingredient firstIngredient = new Ingredient();
    firstIngredient.setName(recipeDto.getIngredients().get(0).getName());
    firstIngredient.setQuantity(recipeDto.getIngredients().get(0).getQuantity());
    firstIngredient.setUnit(recipeDto.getIngredients().get(0).getUnit());

    Ingredient secondIngredient = new Ingredient();
    secondIngredient.setName(recipeDto.getIngredients().get(1).getName());
    secondIngredient.setQuantity(recipeDto.getIngredients().get(1).getQuantity());
    secondIngredient.setUnit(recipeDto.getIngredients().get(1).getUnit());

    entity.setIngredients(List.of(firstIngredient, secondIngredient));

    firstIngredient.setRecipe(entity);
    secondIngredient.setRecipe(entity);
    return entity;
  }
}