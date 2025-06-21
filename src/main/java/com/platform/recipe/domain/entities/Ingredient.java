package com.platform.recipe.domain.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "ingredient")
public class Ingredient {

  @Id
  @Column(name = "id", unique = true, updatable = false, nullable = false)
  private UUID id;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "quantity", nullable = false)
  private Integer quantity;

  @Column(name = "unit", nullable = false)
  private String unit;

  private boolean isOptional;

  @ManyToOne
  @JoinColumn(name = "recipe_id")
  private Recipe recipe;
}
