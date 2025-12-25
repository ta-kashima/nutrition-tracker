package com.example.nutrition.repository;

import com.example.nutrition.model.MealSetFood;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MealSetFoodRepository extends JpaRepository<MealSetFood, Long> {

    List<MealSetFood> findByMealSetSetIdOrderBySortOrderAsc(Long setId);

    void deleteByMealSetSetId(Long setId);
}
