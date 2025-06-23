package com.platform.recipe.domain.repositories;

import com.platform.recipe.domain.entities.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeJpaRepository extends JpaRepository<Recipe, Long>, CustomRecipeJpaRepository {
}
