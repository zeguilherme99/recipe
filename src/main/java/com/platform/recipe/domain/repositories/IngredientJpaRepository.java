package com.platform.recipe.domain.repositories;

import com.platform.recipe.domain.entities.Ingredient;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IngredientJpaRepository extends JpaRepository<Ingredient, Long> {

  List<Ingredient> findByRecipeIdIn(List<Long> recipeIds);
}
