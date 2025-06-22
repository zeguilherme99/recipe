package com.platform.recipe.adapters.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.recipe.adapters.controllers.dtos.request.IngredientRequest;
import com.platform.recipe.adapters.controllers.dtos.request.RecipeRequest;
import com.platform.recipe.domain.dtos.IngredientDto;
import com.platform.recipe.domain.dtos.RecipeDto;
import com.platform.recipe.domain.services.RecipeService;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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

  private static Stream<Arguments> invalidRecipeRequests() {
    return Stream.of(
        Arguments.of(
            new RecipeRequest(
              " ",
              "Some description",
              true,
              "Instructions",
              List.of(new IngredientRequest("Flour", 200, "g"))
            )
        ),
        Arguments.of(
            new RecipeRequest(
                "a",
                "Some description",
                true,
                "Instructions",
                List.of(new IngredientRequest("Flour", 200, "g"))
            )
        ),
        Arguments.of(
            new RecipeRequest(
              "Valid title",
              "Some description",
              true,
              "  ",
              List.of(new IngredientRequest("Flour", 200, "g"))
            )
        ),
        Arguments.of(
          new RecipeRequest(
              "Valid title",
              "Some description",
              true,
              "Instructions",
              List.of(
                  new IngredientRequest("  ", 500, "g"),
                  new IngredientRequest("Bean", 300, "g")
              )
          )
        ),
        Arguments.of(
            new RecipeRequest(
              "Valid title",
              "Some description",
              true,
              "Instructions",
              List.of()
            )
        )
    );
  }

  @Test
  void shouldReturnCreatedWithIdSuccessfully() throws Exception {
    RecipeRequest recipeRequest = createRequest();
    when(recipeService.create(any(RecipeDto.class))).thenReturn(42L);

    mockMvc.perform(post("/v1/recipes").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(recipeRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(42));

    verify(recipeService).create(any(RecipeDto.class));
  }

  @Test
  void shouldUpdateRecipeSuccessfully() throws Exception {
    RecipeRequest request = createRequest();
    RecipeDto dto = createDto();
    when(recipeService.update(any(RecipeDto.class))).thenReturn(dto);

    mockMvc.perform(put("/v1/recipes/{id}", 123L)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(123L))
        .andExpect(jsonPath("$.title").value("Updated Title"))
        .andExpect(jsonPath("$.vegetarian").value(true));

    verify(recipeService).update(any(RecipeDto.class));
  }

  @ParameterizedTest
  @MethodSource("invalidRecipeRequests")
  void shouldReturn400ForInvalidUpdateRequests(RecipeRequest invalidRequest) throws Exception {
    mockMvc.perform(put("/v1/recipes/{id}", 1)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());

    verify(recipeService, never()).update(any(RecipeDto.class));
  }

  @ParameterizedTest
  @MethodSource("invalidRecipeRequests")
  void shouldReturn400ForInvalidCreateRequests(RecipeRequest invalidRequest) throws Exception {
    mockMvc.perform(post("/v1/recipes")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());

    verify(recipeService, never()).create(any(RecipeDto.class));
  }

  @Test
  void shouldDeleteRecipeSuccessfully() throws Exception {
    Long id = 1L;

    mockMvc.perform(delete("/v1/recipes/{id}", id))
        .andExpect(status().isNoContent());

    verify(recipeService).deleteById(id);
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

  private RecipeDto createDto() {
    RecipeDto dto =  new RecipeDto();
    dto.setId(123L);
    dto.setTitle("Updated Title");
    dto.setDescription("Updated Description");
    dto.setVegetarian(true);
    dto.setInstructions("Updated instructions");
    dto.setIngredients(List.of(new IngredientDto(null, "Sugar", 100, "ml")));

    return dto;
  }
}