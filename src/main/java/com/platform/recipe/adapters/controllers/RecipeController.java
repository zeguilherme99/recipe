package com.platform.recipe.adapters.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.recipe.adapters.controllers.dtos.RecipeIdResponse;
import com.platform.recipe.adapters.controllers.dtos.RecipeRequest;
import com.platform.recipe.domain.dtos.RecipeDto;
import com.platform.recipe.domain.services.RecipeService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

  @PostMapping
  public ResponseEntity<RecipeIdResponse> create(@Valid @RequestBody RecipeRequest recipeRequest) {
    log.info("Received request to create recipe [{}]", recipeRequest.getTitle());
    RecipeDto recipeDto = objectMapper.convertValue(recipeRequest, RecipeDto.class);

    Long id = recipeService.create(recipeDto);

    RecipeIdResponse response = new RecipeIdResponse(id);

    log.info("Recipe [{}] successfully created with id [{}]", recipeRequest.getTitle(), id);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
}
