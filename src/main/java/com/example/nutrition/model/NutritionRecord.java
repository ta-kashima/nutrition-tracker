package com.example.nutrition.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

/**
 * 栄養記録エンティティ
 */
@Entity
@Table(name = "nutrition_records")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long recordId;

    @Column(name = "user_id", nullable = false, length = 128)
    @NotBlank(message = "ユーザーIDは必須です")
    private String userId;

    @Column(nullable = false)
    @NotNull(message = "日付は必須です")
    private LocalDate date;

    @Column(name = "meal_type", length = 20)
    @Size(max = 20, message = "食事タイプは20文字以内で入力してください")
    private String mealType;

    @Column(name = "food_id")
    private Long foodId;

    @Column(name = "food_name", length = 200)
    @Size(max = 200, message = "食品名は200文字以内で入力してください")
    private String foodName;

    @PositiveOrZero(message = "グラム数は0以上で入力してください")
    private Double grams;

    @PositiveOrZero(message = "カロリーは0以上で入力してください")
    private Double calories;

    @PositiveOrZero(message = "タンパク質は0以上で入力してください")
    private Double protein;

    @PositiveOrZero(message = "脂質は0以上で入力してください")
    private Double fat;

    @PositiveOrZero(message = "炭水化物は0以上で入力してください")
    private Double carbohydrates;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
