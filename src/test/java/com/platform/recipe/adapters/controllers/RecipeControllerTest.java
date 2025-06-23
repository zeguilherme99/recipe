package com.platform.recipe.adapters.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.recipe.adapters.controllers.dtos.request.IngredientCreateRequest;
import com.platform.recipe.adapters.controllers.dtos.request.IngredientUpdateRequest;
import com.platform.recipe.adapters.controllers.dtos.request.RecipeCreateRequest;
import com.platform.recipe.adapters.controllers.dtos.request.RecipeUpdateRequest;
import com.platform.recipe.adapters.controllers.dtos.response.IngredientResponse;
import com.platform.recipe.adapters.controllers.dtos.response.RecipeIdResponse;
import com.platform.recipe.adapters.controllers.dtos.response.RecipeResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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

  private static Stream<Arguments> invalidRecipeCreateRequests() {
    return Stream.of(
      Arguments.of(
        new RecipeCreateRequest(
          " ",
          "Some description",
          true,
          "Instructions",
          List.of(new IngredientCreateRequest("Flour", 200, "g"))
        )
      ),
      Arguments.of(
        new RecipeCreateRequest(
            "a",
            "Some description",
            true,
            "Instructions",
            List.of(new IngredientCreateRequest("Flour", 200, "g"))
        )
      ),
      Arguments.of(
        new RecipeCreateRequest(
          "Valid title",
          "Some description",
          true,
          "  ",
          List.of(new IngredientCreateRequest("Flour", 200, "g"))
        )
      ),
      Arguments.of(
        new RecipeCreateRequest(
          "Valid title",
          "Some description",
          true,
          "Instructions",
          List.of(
            new IngredientCreateRequest("  ", 500, "g"),
            new IngredientCreateRequest("Bean", 300, "g")
          )
        )
      ),
      Arguments.of(
        new RecipeCreateRequest(
          "Valid title",
          "Some description",
          true,
          "Instructions",
          List.of()
        )
      )
    );
  }

  private static Stream<Arguments> invalidRecipeUpdateRequests() {
    return Stream.of(
      Arguments.of(
        new RecipeUpdateRequest(
          " ",
          "Some description",
          true,
          "Instructions",
          List.of(new IngredientUpdateRequest(1L,"Flour", 200, "g"))
        )
      ),
      Arguments.of(
        new RecipeUpdateRequest(
          "a",
          "Some description",
          true,
          "Instructions",
          List.of(new IngredientUpdateRequest(2L,"Flour", 200, "g"))
        )
      ),
      Arguments.of(
        new RecipeUpdateRequest(
          "Valid title",
          "Some description",
          true,
          "  ",
          List.of(new IngredientUpdateRequest(3L,"Flour", 200, "g"))
        )
      ),
      Arguments.of(
        new RecipeUpdateRequest(
          "Valid title",
          "Some description",
          true,
          "Instructions",
          List.of(
            new IngredientUpdateRequest(4L,"  ", 500, "g"),
            new IngredientUpdateRequest(5L,"Bean", 300, "g")
          )
        )
      ),
      Arguments.of(
        new RecipeUpdateRequest(
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
    Long id = 42L;
    RecipeCreateRequest recipeRequest = createRequest();
    RecipeIdResponse expectedResponse = new RecipeIdResponse(id);
    when(recipeService.create(any(RecipeDto.class))).thenReturn(id);

    mockMvc.perform(post("/v1/recipes").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(recipeRequest)))
        .andExpect(status().isCreated())
        .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)));

    verify(recipeService).create(any(RecipeDto.class));
  }

  @Test
  void shouldUpdateRecipeSuccessfully() throws Exception {
    RecipeUpdateRequest request = createUpdateRequest();
    RecipeDto dto = createDto();
    RecipeResponse expectedResponse = createResponseFromDto(dto);

    when(recipeService.update(any(RecipeDto.class))).thenReturn(dto);

    mockMvc.perform(put("/v1/recipes/{id}", 123L)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)));

    verify(recipeService).update(any(RecipeDto.class));
  }

  @ParameterizedTest
  @MethodSource("invalidRecipeUpdateRequests")
  void shouldReturn400ForInvalidUpdateRequests(RecipeUpdateRequest invalidRequest) throws Exception {
    mockMvc.perform(put("/v1/recipes/{id}", 1)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());

    verify(recipeService, never()).update(any(RecipeDto.class));
  }

  @ParameterizedTest
  @MethodSource("invalidRecipeCreateRequests")
  void shouldReturn400ForInvalidCreateRequests(RecipeCreateRequest invalidRequest) throws Exception {
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

  @Test
  void shouldFindAndReturnRecipeSuccessfully() throws Exception {
    RecipeDto dto = createDto();
    RecipeResponse expectedResponse = createResponseFromDto(dto);

    when(recipeService.findById(dto.getId())).thenReturn(dto);

    mockMvc.perform(get("/v1/recipes/{id}", dto.getId()))
      .andExpect(status().isOk())
      .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)));
  }

  @Test
  void shouldReturn200AndRecipesWhenUsingFilters() throws Exception {

    RecipeDto recipeDto = createDto();
    Page<RecipeDto> page = new PageImpl<>(List.of(recipeDto));
    Page<RecipeResponse> expectedResponse = page.map(recipe -> objectMapper.convertValue(recipe, RecipeResponse.class));

    when(recipeService.searchWithFilters(
      eq(true),
      eq(1),
      eq(List.of("Tomato")),
      eq(List.of("Salt")),
      eq("bake"),
      any(),
      any(),
      eq(0),
      eq(10),
      eq("createdAt")
    )).thenReturn(page);

    mockMvc.perform(get("/v1/recipes")
      .param("vegetarian", "true")
      .param("include", "Tomato")
      .param("exclude", "Salt")
      .param("instruction", "bake")
      .param("page", "0")
      .param("pageSize", "10")
      .param("sort", "createdAt")
      .contentType(MediaType.APPLICATION_JSON))
    .andExpect(status().isOk())
    .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)));

  }

  private RecipeResponse createResponseFromDto(RecipeDto dto) {
    List<IngredientResponse> ingredientResponses = dto.getIngredients()
      .stream()
      .map(ingredientDto -> new IngredientResponse(
        ingredientDto.getId(),
        ingredientDto.getName(),
        ingredientDto.getQuantity(),
        ingredientDto.getUnit()
      ))
      .toList();

    return new RecipeResponse(
      dto.getId(),
      dto.getTitle(),
      dto.getDescription(),
      dto.isVegetarian(),
      dto.getInstructions(),
      ingredientResponses,
      dto.getCreatedAt(),
      dto.getUpdatedAt()
    );
  }

  private RecipeCreateRequest createRequest() {
    return new RecipeCreateRequest(
      "Feijoada",
      "description",
      false,
      "instructions",
      List.of(
        new IngredientCreateRequest("Brad", 500, "g"),
        new IngredientCreateRequest("Bean", 300, "g")
      )
    );
  }

  private RecipeUpdateRequest createUpdateRequest() {
    return new RecipeUpdateRequest(
      "Feijoada",
      "description",
      false,
      "instructions",
      List.of(
        new IngredientUpdateRequest(1L,"Brad", 500, "g"),
        new IngredientUpdateRequest(2L, "Bean", 300, "g")
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
    dto.setIngredients(List.of(new IngredientDto(null, "Sugar", 100, "g")));

    return dto;
  }
}