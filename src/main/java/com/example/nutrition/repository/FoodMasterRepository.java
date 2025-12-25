package com.example.nutrition.repository;

import com.example.nutrition.model.FoodMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodMasterRepository extends JpaRepository<FoodMaster, Long> {

    List<FoodMaster> findByUserIdOrIsPublicTrueOrderByUsageCountDesc(String userId);

    List<FoodMaster> findByIsPublicTrue();

    long countByIsPublicTrue();

    @Modifying
    @Query("DELETE FROM FoodMaster f WHERE f.isPublic = true")
    void deleteByIsPublicTrue();

    List<FoodMaster> findByUserId(String userId);

    @Query("SELECT f FROM FoodMaster f WHERE (f.userId = ?1 OR f.isPublic = true) AND f.mealType = ?2 ORDER BY f.usageCount DESC")
    List<FoodMaster> findByUserIdOrPublicAndMealType(String userId, String mealType);

    List<FoodMaster> findByFoodNameContainingIgnoreCase(String keyword);

    @Query("SELECT f FROM FoodMaster f WHERE f.userId = ?1 OR f.isPublic = true ORDER BY f.foodName ASC")
    List<FoodMaster> findByUserIdOrIsPublicTrueOrderByFoodNameAsc(String userId);

    @Query("SELECT f FROM FoodMaster f WHERE (f.userId = ?1 OR f.isPublic = true) AND LOWER(f.foodName) LIKE LOWER(CONCAT('%', ?2, '%')) ORDER BY f.foodName ASC")
    List<FoodMaster> findByUserIdOrIsPublicTrueAndFoodNameContainingIgnoreCase(String userId, String keyword);
}
