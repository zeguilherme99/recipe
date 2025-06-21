package com.platform.recipe.domain.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "recipe")
@Getter
@Setter
public class Recipe {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "title", nullable = false)
  private String title;

  @Column(name = "description", nullable = false)
  private String description;

  @Column(name = "is_vegetarian", nullable = false)
  private boolean vegetarian;

  @Column(name = "instructions", columnDefinition = "TEXT", nullable = false)
  private String instructions;

  @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Ingredient> ingredients = new ArrayList<>();

  @CreationTimestamp
  @Column(name = "created_at", nullable = false)
  Timestamp createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  Timestamp updatedAt;
}
