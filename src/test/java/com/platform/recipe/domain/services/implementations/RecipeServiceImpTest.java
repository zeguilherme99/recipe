package com.platform.recipe.domain.services.implementations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.recipe.domain.dtos.IngredientDto;
import com.platform.recipe.domain.dtos.RecipeDto;
import com.platform.recipe.domain.entities.Ingredient;
import com.platform.recipe.domain.entities.Recipe;
import com.platform.recipe.domain.exceptions.DataNotFoundException;
import com.platform.recipe.domain.exceptions.ErrorCode;
import com.platform.recipe.domain.repositories.IngredientJpaRepository;
import com.platform.recipe.domain.repositories.RecipeJpaRepository;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class RecipeServiceImpTest {

  @InjectMocks
  private RecipeServiceImp recipeService;

  @Mock
  private RecipeJpaRepository recipeJpaRepository;

  @Mock
  private IngredientJpaRepository ingredientJpaRepository;

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
  void shouldThrowDataNotFoundExceptionWhenRecipeNotFoundUpdate() {
    Long id = 99L;
    RecipeDto dto = new RecipeDto();
    dto.setId(id);

    when(recipeJpaRepository.findById(id)).thenReturn(Optional.empty());

    DataNotFoundException exception = assertThrows(DataNotFoundException.class, () -> recipeService.update(dto));

    verify(recipeJpaRepository, never()).save(any());
    assertEquals(ErrorCode.RECIPE_NOT_FOUND, exception.getErrorCode());

  }

  @Test
  void shouldDeleteRecipeSuccessfully() throws DataNotFoundException {
    Long id = 1L;

    when(recipeJpaRepository.existsById(id)).thenReturn(true);

    recipeService.deleteById(id);

    verify(recipeJpaRepository).deleteById(id);
  }

  @Test
  void shouldThrowDataNotFoundExceptionWhenRecipeNotFoundDelete() {
    Long id = 999L;

    when(recipeJpaRepository.existsById(id)).thenReturn(false);

    DataNotFoundException exception = assertThrows(DataNotFoundException.class, () -> recipeService.deleteById(id));

    verify(recipeJpaRepository, never()).deleteById(any());
    assertEquals(ErrorCode.RECIPE_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  void shouldReturnRecipeDtoWhenFound() throws Exception {
    Long id = 1L;

    RecipeDto recipeDto = createDto();
    recipeDto.setId(id);
    Recipe recipeEntity = createRecipe(recipeDto);

    when(recipeJpaRepository.findById(id)).thenReturn(Optional.of(recipeEntity));
    when(objectMapper.convertValue(recipeEntity, RecipeDto.class)).thenReturn(recipeDto);

    RecipeDto result = recipeService.findById(id);

    assertEquals(id, result.getId());
    assertEquals(recipeEntity.getTitle(), result.getTitle());
    verify(recipeJpaRepository).findById(id);
  }

  @Test
  void shouldThrowDataNotFoundExceptionWhenRecipeDoesNotExist() {
    Long id = 99L;
    when(recipeJpaRepository.findById(id)).thenReturn(Optional.empty());

    DataNotFoundException exception = assertThrows(
      DataNotFoundException.class,
      () -> recipeService.findById(id)
    );

    assertEquals(ErrorCode.RECIPE_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  void shouldSearchWithFiltersAndConvertPage() {
    Boolean vegetarian = true;
    int serving = 2;
    List<String> includedIngredients = List.of("Salt", "Pepper");
    List<String> excludedIngredients = List.of("Sugar");
    String instruction = "mix";
    Instant createdAfter = Instant.parse("2023-01-01T00:00:00Z");
    Instant createdBefore = Instant.parse("2023-12-31T23:59:59Z");
    int page = 0;
    int pageSize = 10;
    String sort = "createdAt";

    RecipeDto firstRecipeDto = createDto();
    Recipe firstRecipe = createRecipe(firstRecipeDto);
    firstRecipe.setId(1L);

    RecipeDto secondRecipeDto = createDto();
    Recipe secondRecipe = createRecipe(secondRecipeDto);
    secondRecipe.setId(2L);

    List<Recipe> recipes = List.of(firstRecipe, secondRecipe);

    Page<Recipe> recipePage = new PageImpl<>(recipes, PageRequest.of(page, pageSize, Sort.by(sort).descending()), 2);

    Ingredient firstIngredient = new Ingredient();
    firstIngredient.setRecipe(firstRecipe);
    firstIngredient.setQuantity(1);
    Ingredient secondIngredient = new Ingredient();
    secondIngredient.setRecipe(secondRecipe);
    secondIngredient.setQuantity(3);

    List<Ingredient> ingredients = List.of(firstIngredient, secondIngredient);

    when(recipeJpaRepository.searchWithFilters(
      eq(vegetarian),
      eq(includedIngredients),
      eq(excludedIngredients),
      eq(instruction),
      eq(createdAfter),
      eq(createdBefore),
      any(Pageable.class)
    )).thenReturn(recipePage);

    when(ingredientJpaRepository.findByRecipeIdIn(List.of(1L, 2L))).thenReturn(ingredients);

    when(objectMapper.convertValue(any(Recipe.class), eq(RecipeDto.class))).thenAnswer(invocation -> {
      Recipe r = invocation.getArgument(0);
      RecipeDto dto = new RecipeDto();
      dto.setId(r.getId());
      dto.setIngredients(r.getIngredients().stream()
        .map(i -> {
          IngredientDto ingredientDto = new IngredientDto();
          ingredientDto.setQuantity(i.getQuantity());
          return ingredientDto;
        }).collect(Collectors.toList())
      );
      return dto;
    });

    Page<RecipeDto> result = recipeService.searchWithFilters(
      vegetarian,
      serving,
      includedIngredients,
      excludedIngredients,
      instruction,
      createdAfter,
      createdBefore,
      page,
      pageSize,
      sort
    );

    assertNotNull(result);
    assertEquals(2, result.getContent().size());

    RecipeDto firstDto = result.getContent().get(0);
    assertEquals(2, firstDto.getIngredients().get(0).getQuantity());

    RecipeDto secondDto = result.getContent().get(1);
    assertEquals(6, secondDto.getIngredients().get(0).getQuantity());

    verify(recipeJpaRepository).searchWithFilters(
      eq(vegetarian),
      eq(includedIngredients),
      eq(excludedIngredients),
      eq(instruction),
      eq(createdAfter),
      eq(createdBefore),
      argThat(pageable -> pageable.getPageNumber() == page
        && pageable.getPageSize() == pageSize
        && pageable.getSort().getOrderFor("createdAt").isDescending())
    );

    verify(ingredientJpaRepository).findByRecipeIdIn(List.of(1L, 2L));
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