package com.platform.recipe.adapters.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.recipe.adapters.controllers.dtos.IngredientRequest;
import com.platform.recipe.adapters.controllers.dtos.RecipeRequest;
import com.platform.recipe.domain.dtos.RecipeDto;
import com.platform.recipe.domain.services.RecipeService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RecipeController.class)
class RecipeControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private RecipeService recipeService;

  @Test
  void shouldReturnCreatedWithId() throws Exception {
    RecipeRequest recipeRequest = createRequest();
    when(recipeService.create(any(RecipeDto.class))).thenReturn(42L);

    mockMvc.perform(post("/v1/recipes").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(recipeRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(42));
  }

  @Test
  void shouldReturn400WhenTitleIsBlank() throws Exception {
    RecipeRequest request = createRequest();
    request.setTitle("  ");

    mockMvc.perform(post("/v1/recipes")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturn400WhenDescriptionTooShort() throws Exception {
    RecipeRequest request = createRequest();
    request.setDescription("a");

    mockMvc.perform(post("/v1/recipes")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturn400WhenInstructionsIsBlank() throws Exception {
    RecipeRequest request = createRequest();
    request.setInstructions("  ");

    mockMvc.perform(post("/v1/recipes")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturn400WhenIngredientsIsNull() throws Exception {
    RecipeRequest request = createRequest();
    request.setIngredients(null);

    mockMvc.perform(post("/v1/recipes")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturn400WhenIngredientsNameIsBlank() throws Exception {
    RecipeRequest request = createRequest();
    request.setIngredients(List.of(
        new IngredientRequest("  ", 500, "g"),
        new IngredientRequest("Bean", 300, "g")
    ));

    mockMvc.perform(post("/v1/recipes")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturn400WhenIngredientsQuantityIsSmallerThanZero() throws Exception {
    RecipeRequest request = createRequest();
    request.setIngredients(List.of(
        new IngredientRequest("Brad", 500, "g"),
        new IngredientRequest("Bean", -1, "g")
    ));

    mockMvc.perform(post("/v1/recipes")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  private RecipeRequest createRequest() {
    return new RecipeRequest(
        "Feijoada",
        "description",
        false,
        "instructions",
        List.of(
            new IngredientRequest("Brad", 500, "g"),
            new IngredientRequest("Bean", 300, "g")
        )
    );
  }
}