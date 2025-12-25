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

/**
 * 食事セット明細エンティティ
 * 食事セットに含まれる食品
 */
@Entity
@Table(name = "meal_set_foods")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealSetFood {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "set_id", nullable = false)
    private MealSet mealSet;

    @Column(name = "food_id")
    private Long foodId;

    @Column(name = "food_name", nullable = false, length = 200)
    @NotBlank(message = "食品名は必須です")
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

    @Column(name = "sort_order")
    @PositiveOrZero(message = "並び順は0以上で入力してください")
    private Integer sortOrder;
}
