package com.example.nutrition.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 食事セットエンティティ
 * よく食べる食事の組み合わせを保存
 */
@Entity
@Table(name = "meal_sets")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "set_id")
    private Long setId;

    @Column(name = "user_id", nullable = false, length = 128)
    @NotBlank(message = "ユーザーIDは必須です")
    private String userId;

    @Column(name = "set_name", nullable = false, length = 100)
    @NotBlank(message = "セット名は必須です")
    @Size(max = 100, message = "セット名は100文字以内で入力してください")
    private String setName;

    @Column(name = "meal_type", length = 20)
    @Size(max = 20, message = "食事タイプは20文字以内で入力してください")
    private String mealType;

    @Column(length = 500)
    @Size(max = 500, message = "説明は500文字以内で入力してください")
    private String description;

    @Column(name = "total_calories")
    private Double totalCalories;

    @Column(name = "total_protein")
    private Double totalProtein;

    @Column(name = "total_fat")
    private Double totalFat;

    @Column(name = "total_carbs")
    private Double totalCarbs;

    @Column(name = "usage_count")
    private Integer usageCount;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "mealSet", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MealSetFood> foods = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (usageCount == null) usageCount = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    /**
     * 栄養素合計を再計算
     */
    public void recalculateTotals() {
        totalCalories = 0.0;
        totalProtein = 0.0;
        totalFat = 0.0;
        totalCarbs = 0.0;

        if (foods == null) {
            return;
        }

        for (MealSetFood food : foods) {
            if (food.getCalories() != null) totalCalories += food.getCalories();
            if (food.getProtein() != null) totalProtein += food.getProtein();
            if (food.getFat() != null) totalFat += food.getFat();
            if (food.getCarbohydrates() != null) totalCarbs += food.getCarbohydrates();
        }
    }
}
