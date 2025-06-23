package com.platform.recipe.domain.repositories.implementations;

import com.platform.recipe.domain.entities.Ingredient;
import com.platform.recipe.domain.entities.Recipe;
import com.platform.recipe.domain.repositories.CustomRecipeJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class CustomRecipeJpaRepositoryImpl implements CustomRecipeJpaRepository {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Page<Recipe> searchWithFilters(
    Boolean vegetarian,
    List<String> includedIngredients,
    List<String> excludedIngredients,
    String instruction,
    Instant createdAfter,
    Instant createdBefore,
    Pageable pageable
  ) {

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Recipe> criteriaQuery = criteriaBuilder.createQuery(Recipe.class);
    Root<Recipe> root = criteriaQuery.from(Recipe.class);

    Predicate predicate = buildPredicate(
      criteriaBuilder,
      root,
      vegetarian,
      includedIngredients,
      excludedIngredients,
      instruction,
      createdAfter,
      createdBefore
    );

    criteriaQuery.select(root).where(predicate);

    if (!pageable.getSort().isEmpty()) {
      List<Order> orders = pageable.getSort().stream()
        .map(order -> order.isAscending()
          ? criteriaBuilder.asc(root.get(order.getProperty()))
          : criteriaBuilder.desc(root.get(order.getProperty())))
        .toList();
      criteriaQuery.orderBy(orders);
    }

    TypedQuery<Recipe> query = entityManager.createQuery(criteriaQuery);
    query.setFirstResult((int) pageable.getOffset());
    query.setMaxResults(pageable.getPageSize());

    CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
    Root<Recipe> countRoot = countQuery.from(Recipe.class);
    Predicate countPredicate = buildPredicate(
      criteriaBuilder,
      countRoot,
      vegetarian,
      includedIngredients,
      excludedIngredients,
      instruction,
      createdAfter,
      createdBefore
    );
    countQuery.select(criteriaBuilder.count(countRoot)).where(countPredicate);
    Long total = entityManager.createQuery(countQuery).getSingleResult();

    return new PageImpl<>(query.getResultList(), pageable, total);
  }

  private Predicate buildPredicate(
    CriteriaBuilder cb,
    Root<Recipe> root,
    Boolean vegetarian,
    List<String> includedIngredients,
    List<String> excludedIngredients,
    String instruction,
    Instant createdAfter,
    Instant createdBefore
  ) {
    Predicate predicate = cb.conjunction();

    if (vegetarian != null) {
      predicate = cb.and(predicate, cb.equal(root.get("vegetarian"), vegetarian));
    }

    if (instruction != null && !instruction.isBlank()) {
      predicate = cb.and(predicate, cb.like(root.get("instructions"), "%" + instruction.toLowerCase() + "%"));
    }

    if (createdAfter != null) {
      predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("createdAt"), Timestamp.from(createdAfter)));
    }

    if (createdBefore != null) {
      predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("createdAt"), Timestamp.from(createdBefore)));
    }

    if (includedIngredients != null && !includedIngredients.isEmpty() ) {
      Join<Recipe, Ingredient> ingredientJoin = root.join("ingredients", JoinType.LEFT);
      predicate = cb.and(predicate, ingredientJoin.get("name").in(includedIngredients));
    }

    if (excludedIngredients != null && !excludedIngredients.isEmpty() ) {
      Subquery<Long> subquery = cb.createQuery().subquery(Long.class);
      Root<Ingredient> ingredientRoot = subquery.from(Ingredient.class);
      subquery.select(cb.literal(1L));
      subquery.where(
        cb.and(
          cb.equal(ingredientRoot.get("recipe"), root),
          ingredientRoot.get("name").in(excludedIngredients)
        )
      );

      predicate = cb.and(predicate, cb.not(cb.exists(subquery)));
    }

    return predicate;
  }
}
