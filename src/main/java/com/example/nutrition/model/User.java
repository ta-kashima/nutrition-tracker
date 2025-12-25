package com.example.nutrition.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * ユーザーエンティティ
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @Column(name = "user_id", length = 128)
    @NotBlank(message = "ユーザーIDは必須です")
    private String userId;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "メールアドレスは必須です")
    @Email(message = "正しいメールアドレス形式で入力してください")
    private String email;

    @Column(length = 100)
    @Size(max = 100, message = "名前は100文字以内で入力してください")
    private String name;

    @Min(value = 1, message = "年齢は1以上で入力してください")
    @Max(value = 150, message = "年齢は150以下で入力してください")
    private Integer age;

    @Column(length = 10)
    @Size(max = 10, message = "性別は10文字以内で入力してください")
    private String gender;

    @PositiveOrZero(message = "身長は0以上で入力してください")
    private Double height;

    @PositiveOrZero(message = "体重は0以上で入力してください")
    private Double weight;

    @Column(name = "activity_level", length = 20)
    @Size(max = 20, message = "活動レベルは20文字以内で入力してください")
    private String activityLevel;

    @Column(name = "target_calories")
    @PositiveOrZero(message = "目標カロリーは0以上で入力してください")
    private Integer targetCalories;

    @Column(name = "target_protein")
    @PositiveOrZero(message = "目標タンパク質は0以上で入力してください")
    private Integer targetProtein;

    @Column(name = "target_fat")
    @PositiveOrZero(message = "目標脂質は0以上で入力してください")
    private Integer targetFat;

    @Column(name = "target_carbs")
    @PositiveOrZero(message = "目標炭水化物は0以上で入力してください")
    private Integer targetCarbs;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
