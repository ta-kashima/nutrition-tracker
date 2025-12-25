package com.example.nutrition.repository;

import com.example.nutrition.model.NutritionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface NutritionRecordRepository extends JpaRepository<NutritionRecord, Long> {

    List<NutritionRecord> findByUserIdAndDate(String userId, LocalDate date);

    List<NutritionRecord> findByUserIdAndDateAndMealType(String userId, LocalDate date, String mealType);

    List<NutritionRecord> findByUserIdAndDateBetweenOrderByDateAsc(String userId, LocalDate startDate, LocalDate endDate);

    List<NutritionRecord> findByUserIdOrderByDateDescCreatedAtDesc(String userId);
}
