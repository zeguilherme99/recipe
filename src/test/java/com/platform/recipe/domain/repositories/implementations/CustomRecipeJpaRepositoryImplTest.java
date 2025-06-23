package com.platform.recipe.domain.repositories.implementations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.platform.recipe.domain.entities.Ingredient;
import com.platform.recipe.domain.entities.Recipe;
import com.platform.recipe.domain.repositories.CustomRecipeJpaRepository;
import com.platform.recipe.domain.repositories.RecipeJpaRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CustomRecipeJpaRepositoryImplTest {

  @Autowired
  @Qualifier("customRecipeJpaRepositoryImpl")
  private CustomRecipeJpaRepository customRecipeJpaRepository;

  @Autowired
  private RecipeJpaRepository recipeJpaRepository;

  @BeforeEach
  void setUp() {
    recipeJpaRepository.deleteAll();

    Recipe recipe1 = createRecipe("Feijoada", "Cook for 2 hours", "FirstDescription", false);
    Ingredient ing1 = createIngredient("Bean", 500, "g", recipe1);
    Ingredient ing2 = createIngredient("Milk", 300, "g", recipe1);
    recipe1.setIngredients(List.of(ing1, ing2));

    Recipe recipe2 = createRecipe("Salad", "Instructions", "Light", false);
    Ingredient ing3 = createIngredient("Lettuce", 100, "g", recipe2);
    Ingredient ing4 = createIngredient("Tomato", 80, "g", recipe2);
    recipe2.setIngredients(List.of(ing3, ing4));

    Recipe recipe3 = createRecipe("Tropeiro beans", "Cook and mix everything", "Vegetarian version", true);
    Ingredient ing5 = createIngredient("bean", 50, "g", recipe3);
    Ingredient ing6 = createIngredient("Cassava flour", 80, "g", recipe3);
    recipe3.setIngredients(List.of(ing5, ing6));

    recipeJpaRepository.saveAll(List.of(recipe1, recipe2, recipe3));
  }

  @Test
  void shouldReturnOnlyVegetarianRecipesWithBeanSuccessfully() {
    Page<Recipe> result = customRecipeJpaRepository.searchWithFilters(
      true,
      List.of("bean"),
      null,
      null,
      null,
      null,
      PageRequest.of(0, 10, Sort.by("createdAt").descending())
    );

    assertNotNull(result);
    assertEquals(result.getTotalElements(), 1);
    Recipe recipe = result.getContent().get(0);
    assertEquals(recipe.getTitle(), "Tropeiro beans");
    assertTrue(recipe.isVegetarian());
  }

  @Test
  void shouldFilterByCreatedAfterAndBeforeSuccessfully() {
    Instant createdAfter = Instant.now().minus(2, ChronoUnit.DAYS);
    Instant createdBefore = Instant.now().plus(2, ChronoUnit.DAYS);

    Page<Recipe> result = customRecipeJpaRepository.searchWithFilters(
      null,
      null,
      null,
      null,
      createdAfter,
      createdBefore,
      PageRequest.of(0, 10, Sort.by("createdAt").ascending())
    );

    assertEquals(3, result.getTotalElements());

    List<String> titles = result.stream().map(Recipe::getTitle).toList();
    assertTrue(titles.contains("Feijoada"));
    assertTrue(titles.contains("Salad"));
    assertTrue(titles.contains("Tropeiro beans"));
  }

  @Test
  void shouldReturnRecipesWithInstructionSuccessfully() {

    Page<Recipe> result = customRecipeJpaRepository.searchWithFilters(
      null,
      null,
      null,
      "mix",
      null,
      null,
      PageRequest.of(0, 10)
    );

    assertNotNull(result);
    assertEquals(result.getTotalElements(), 1);
    assertEquals(result.getContent().get(0).getTitle(), "Tropeiro beans");
  }

  @Test
  void shouldReturnRecipesWithExcludedIngredientsSuccessfully() {

    Page<Recipe> result = customRecipeJpaRepository.searchWithFilters(
        null,
        null,
        List.of("Lettuce"),
        null,
        null,
        null,
        PageRequest.of(0, 10)
    );

    assertNotNull(result);
    assertEquals(result.getTotalElements(), 2);
    List<String> titles = result.stream().map(Recipe::getTitle).toList();
    assertTrue(titles.contains("Feijoada"));
    assertTrue(titles.contains("Tropeiro beans"));
  }

  private Recipe createRecipe(
    String title,
    String instructions,
    String description,
    boolean vegetarian
  ) {
    Recipe recipe = new Recipe();
    recipe.setTitle(title);
    recipe.setDescription(description);
    recipe.setVegetarian(vegetarian);
    recipe.setInstructions(instructions);
    return recipe;
  }

  private Ingredient createIngredient(String name, int quantity, String unit, Recipe recipe) {
    Ingredient ingredient = new Ingredient();
    ingredient.setName(name);
    ingredient.setQuantity(quantity);
    ingredient.setUnit(unit);
    ingredient.setRecipe(recipe);
    return ingredient;
  }
}