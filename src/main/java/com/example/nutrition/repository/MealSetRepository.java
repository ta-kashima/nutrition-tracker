package com.example.nutrition.repository;

import com.example.nutrition.model.MealSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MealSetRepository extends JpaRepository<MealSet, Long> {

    /**
     * ユーザーIDで食事セット一覧を取得（N+1対策: 食品リストをJOIN FETCH）
     */
    @Query("SELECT DISTINCT m FROM MealSet m LEFT JOIN FETCH m.foods WHERE m.userId = ?1 ORDER BY m.usageCount DESC")
    List<MealSet> findByUserIdOrderByUsageCountDesc(String userId);

    /**
     * ユーザーIDと食事タイプで食事セット一覧を取得（N+1対策）
     */
    @Query("SELECT DISTINCT m FROM MealSet m LEFT JOIN FETCH m.foods WHERE m.userId = ?1 AND m.mealType = ?2 ORDER BY m.usageCount DESC")
    List<MealSet> findByUserIdAndMealTypeOrderByUsageCountDesc(String userId, String mealType);

    /**
     * ユーザーIDとキーワードで検索（N+1対策）
     */
    @Query("SELECT DISTINCT m FROM MealSet m LEFT JOIN FETCH m.foods WHERE m.userId = ?1 AND LOWER(m.setName) LIKE LOWER(CONCAT('%', ?2, '%')) ORDER BY m.usageCount DESC")
    List<MealSet> findByUserIdAndSetNameContainingIgnoreCase(String userId, String keyword);

    /**
     * ユーザーIDで食事セット一覧を取得（名前順、N+1対策）
     */
    @Query("SELECT DISTINCT m FROM MealSet m LEFT JOIN FETCH m.foods WHERE m.userId = ?1 ORDER BY m.setName ASC")
    List<MealSet> findByUserIdOrderBySetNameAsc(String userId);

    /**
     * IDで食事セットを取得（N+1対策）
     */
    @Query("SELECT m FROM MealSet m LEFT JOIN FETCH m.foods WHERE m.setId = ?1")
    Optional<MealSet> findByIdWithFoods(Long id);
}
