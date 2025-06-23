package com.platform.recipe.adapters.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.recipe.adapters.controllers.config.ResponseError;
import com.platform.recipe.adapters.controllers.dtos.request.RecipeCreateRequest;
import com.platform.recipe.adapters.controllers.dtos.request.RecipeUpdateRequest;
import com.platform.recipe.adapters.controllers.dtos.response.RecipeIdResponse;
import com.platform.recipe.adapters.controllers.dtos.response.RecipeResponse;
import com.platform.recipe.domain.dtos.RecipeDto;
import com.platform.recipe.domain.exceptions.DataNotFoundException;
import com.platform.recipe.domain.services.RecipeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/recipes")
public class RecipeController {

  private final RecipeService recipeService;
  private final ObjectMapper objectMapper;

  public RecipeController(RecipeService recipeService, ObjectMapper objectMapper) {
    this.recipeService = recipeService;
    this.objectMapper = objectMapper;
  }

  @Operation(summary = "Create recipes", description = "Create recipes")
  @ApiResponse(responseCode = "201", description = "Success")
  @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseError.ResponseErrorMessage.class)))
  @ApiResponse(responseCode = "500", description = "Unexpected error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseError.ResponseErrorMessage.class)))
  @PostMapping
  public ResponseEntity<RecipeIdResponse> create(@Valid @RequestBody RecipeCreateRequest recipeRequest) {
    log.info("Received request to create recipe [{}]", recipeRequest.getTitle());
    RecipeDto recipeDto = objectMapper.convertValue(recipeRequest, RecipeDto.class);

    Long id = recipeService.create(recipeDto);

    RecipeIdResponse response = new RecipeIdResponse(id);

    log.info("Recipe [{}] successfully created with id [{}]", recipeRequest.getTitle(), id);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Operation(summary = "Update recipes id", description = "Update recipes with id")
  @ApiResponse(responseCode = "200", description = "Success")
  @ApiResponse(responseCode = "400", description = "Invalid data error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseError.ResponseErrorMessage.class)))
  @ApiResponse(responseCode = "404", description = "Not found error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseError.ResponseErrorMessage.class)))
  @ApiResponse(responseCode = "500", description = "Unexpected error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseError.ResponseErrorMessage.class)))
  @PutMapping("/{id}")
  public ResponseEntity<RecipeResponse> update(
    @PathVariable Long id,
    @Valid @RequestBody RecipeUpdateRequest recipeRequest
  ) throws DataNotFoundException {
    log.info("Received request to update recipe [{}]", id);
    RecipeDto recipeDto = objectMapper.convertValue(recipeRequest, RecipeDto.class);
    recipeDto.setId(id);

    RecipeDto result = recipeService.update(recipeDto);
    RecipeResponse response = objectMapper.convertValue(result, RecipeResponse.class);

    log.info("Recipe with id [{}] successfully updated", id);
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  @Operation(summary = "Delete recipes id", description = "Delete recipes with id")
  @ApiResponse(responseCode = "204", description = "Success")
  @ApiResponse(responseCode = "400", description = "Invalid data error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseError.ResponseErrorMessage.class)))
  @ApiResponse(responseCode = "404", description = "Not found error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseError.ResponseErrorMessage.class)))
  @ApiResponse(responseCode = "500", description = "Unexpected error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseError.ResponseErrorMessage.class)))
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(
    @PathVariable Long id
  ) throws DataNotFoundException {
    log.info("Received request to delete recipe [{}]", id);

    recipeService.deleteById(id);

    log.info("Recipe with id [{}] successfully deleted", id);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Search recipes id", description = "Search recipes with id")
  @ApiResponse(responseCode = "200", description = "Success")
  @ApiResponse(responseCode = "400", description = "Invalid data error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseError.ResponseErrorMessage.class)))
  @ApiResponse(responseCode = "404", description = "Not found error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseError.ResponseErrorMessage.class)))
  @ApiResponse(responseCode = "500", description = "Unexpected error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseError.ResponseErrorMessage.class)))
  @GetMapping("/{id}")
  public ResponseEntity<RecipeResponse> findById(
    @PathVariable Long id
  ) throws DataNotFoundException {
    log.info("Received request to find recipe [{}]", id);

    RecipeDto recipeDto = recipeService.findById(id);
    RecipeResponse response = objectMapper.convertValue(recipeDto, RecipeResponse.class);

    log.info("Recipe with id [{}] successfully found", id);
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  @Operation(summary = "Search for recipes", description = "Search recipes with optional filters")
  @ApiResponse(responseCode = "200", description = "Success")
  @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseError.ResponseErrorMessage.class)))
  @ApiResponse(responseCode = "500", description = "Unexpected error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseError.ResponseErrorMessage.class)))
  @GetMapping
  public ResponseEntity<Page<RecipeResponse>> search(
    @RequestParam(required = false) Boolean vegetarian,
    @RequestParam(required = false, defaultValue = "1") int servings,
    @RequestParam(required = false) List<String> includedIngredients,
    @RequestParam(required = false) List<String> excludedIngredients,
    @RequestParam(required = false) String instruction,
    @RequestParam(required = false) Instant createdAfter,
    @RequestParam(required = false) Instant createdBefore,
    @RequestParam(required = false, defaultValue = "0") int page,
    @RequestParam(required = false, defaultValue = "10") int pageSize,
    @RequestParam(required = false, defaultValue = "createdAt") String sort
  ) {
    log.info("Received request to serch recipe, filters: vegetarian: [{}], serving: [{}], include: [{}],"
      + " exclude: [{}], instruction: [{}], createdAfter: [{}], createdBefore: [{}]",
        vegetarian, servings, includedIngredients, excludedIngredients, instruction, createdAfter, createdBefore);

    Page<RecipeDto> recipes = recipeService.searchWithFilters(
      vegetarian,
      servings,
      includedIngredients,
      excludedIngredients,
      instruction,
      createdAfter,
      createdBefore,
      page,
      pageSize,
      sort
    );

    Page<RecipeResponse> response = recipes.map(dto -> objectMapper.convertValue(dto, RecipeResponse.class));

    log.info("Returning recipe page successfully, filters: vegetarian: [{}], serving: [{}], include: [{}],"
      + " exclude: [{}], instruction: [{}], createdAfter: [{}], createdBefore: [{}]",
        vegetarian, servings, includedIngredients, excludedIngredients, instruction, createdAfter, createdBefore);
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }
}
