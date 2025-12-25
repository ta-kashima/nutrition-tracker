package com.example.nutrition.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * 食品マスタエンティティ
 */
@Entity
@Table(name = "food_master")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "food_id")
    private Long foodId;

    @Column(name = "user_id", length = 128)
    private String userId;

    @Column(name = "food_name", nullable = false, length = 200)
    @NotBlank(message = "食品名は必須です")
    @Size(max = 200, message = "食品名は200文字以内で入力してください")
    private String foodName;

    @Column(name = "meal_type", length = 20)
    @Size(max = 20, message = "食事タイプは20文字以内で入力してください")
    private String mealType;

    @Column(name = "portion_grams")
    @PositiveOrZero(message = "グラム数は0以上で入力してください")
    private Double portionGrams;

    @PositiveOrZero(message = "カロリーは0以上で入力してください")
    private Double calories;

    @PositiveOrZero(message = "タンパク質は0以上で入力してください")
    private Double protein;

    @PositiveOrZero(message = "脂質は0以上で入力してください")
    private Double fat;

    @PositiveOrZero(message = "炭水化物は0以上で入力してください")
    private Double carbohydrates;

    @Column(name = "is_favorite")
    private Boolean isFavorite;

    @Column(name = "usage_count")
    private Integer usageCount;

    @Column(name = "is_public")
    private Boolean isPublic;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (isFavorite == null) isFavorite = false;
        if (usageCount == null) usageCount = 0;
        if (isPublic == null) isPublic = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
