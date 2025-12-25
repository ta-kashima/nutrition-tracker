package com.example.nutrition.service;

import com.example.nutrition.constant.NutritionDefaults;
import com.example.nutrition.exception.AccessDeniedException;
import com.example.nutrition.exception.ResourceNotFoundException;
import com.example.nutrition.exception.ValidationException;
import com.example.nutrition.model.NutritionRecord;
import com.example.nutrition.repository.NutritionRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

/**
 * 栄養記録サービス
 * 食事記録の作成・取得・削除・統計計算を担当
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NutritionRecordService {

    private final NutritionRecordRepository recordRepository;
    private final FoodMasterService foodMasterService;

    /**
     * ユーザーIDと日付で記録を取得
     */
    @Transactional(readOnly = true)
    public List<NutritionRecord> getRecordsByUserIdAndDate(String userId, LocalDate date) {
        validateUserId(userId);
        Objects.requireNonNull(date, "日付は必須です");
        return recordRepository.findByUserIdAndDate(userId, date);
    }

    /**
     * ユーザーIDと日付と食事タイプで記録を取得
     */
    @Transactional(readOnly = true)
    public List<NutritionRecord> getRecordsByUserIdAndDateAndMealType(String userId, LocalDate date, String mealType) {
        if (mealType == null || mealType.isEmpty() || "all".equals(mealType)) {
            return getRecordsByUserIdAndDate(userId, date);
        }
        validateUserId(userId);
        return recordRepository.findByUserIdAndDateAndMealType(userId, date, mealType);
    }

    /**
     * ユーザーIDで全記録を取得
     */
    @Transactional(readOnly = true)
    public List<NutritionRecord> getRecordsByUserId(String userId) {
        validateUserId(userId);
        return recordRepository.findByUserIdOrderByDateDescCreatedAtDesc(userId);
    }

    /**
     * 記録を作成
     */
    @Transactional
    public NutritionRecord createRecord(NutritionRecord record) {
        Objects.requireNonNull(record, "記録データは必須です");
        validateUserId(record.getUserId());

        NutritionRecord saved = recordRepository.save(record);

        // 食品マスタの使用回数をインクリメント
        if (record.getFoodId() != null) {
            try {
                foodMasterService.incrementUsageCount(record.getFoodId());
            } catch (Exception e) {
                log.warn("Failed to increment usage count for food {}: {}", record.getFoodId(), e.getMessage());
            }
        }

        return saved;
    }

    /**
     * 記録を削除（所有者確認付き）
     */
    @Transactional
    public void deleteRecord(Long id, String userId) {
        Objects.requireNonNull(id, "記録IDは必須です");
        validateUserId(userId);

        NutritionRecord record = recordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("記録", id));

        // 所有者確認
        if (!Objects.equals(record.getUserId(), userId)) {
            throw new AccessDeniedException("記録", "削除");
        }

        recordRepository.deleteById(id);
    }

    /**
     * 日別サマリーを取得
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getDailySummaryByUserId(String userId, LocalDate date) {
        List<NutritionRecord> records = getRecordsByUserIdAndDate(userId, date);

        double totalCalories = 0;
        double totalProtein = 0;
        double totalFat = 0;
        double totalCarbs = 0;

        Map<String, List<NutritionRecord>> meals = new LinkedHashMap<>();
        meals.put("breakfast", new ArrayList<>());
        meals.put("lunch", new ArrayList<>());
        meals.put("dinner", new ArrayList<>());
        meals.put("snack", new ArrayList<>());

        for (NutritionRecord record : records) {
            totalCalories += record.getCalories() != null ? record.getCalories() : 0;
            totalProtein += record.getProtein() != null ? record.getProtein() : 0;
            totalFat += record.getFat() != null ? record.getFat() : 0;
            totalCarbs += record.getCarbohydrates() != null ? record.getCarbohydrates() : 0;

            String mealType = record.getMealType();
            if (mealType != null && meals.containsKey(mealType)) {
                meals.get(mealType).add(record);
            } else {
                meals.get("snack").add(record);
            }
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("date", date);
        summary.put("totalCalories", Math.round(totalCalories));
        summary.put("totalProtein", roundToOneDecimal(totalProtein));
        summary.put("totalFat", roundToOneDecimal(totalFat));
        summary.put("totalCarbs", roundToOneDecimal(totalCarbs));
        summary.put("meals", meals);
        summary.put("recordCount", records.size());

        return summary;
    }

    /**
     * 週間統計を取得
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getWeeklyStatisticsByUserId(String userId, LocalDate endDate) {
        validateUserId(userId);
        Objects.requireNonNull(endDate, "終了日は必須です");

        LocalDate startDate = endDate.minusDays(NutritionDefaults.WEEKLY_STATS_DAYS - 1);
        List<NutritionRecord> records = recordRepository.findByUserIdAndDateBetweenOrderByDateAsc(
                userId, startDate, endDate);

        Map<LocalDate, Map<String, Double>> dailyData = new TreeMap<>();
        for (int i = 0; i < NutritionDefaults.WEEKLY_STATS_DAYS; i++) {
            LocalDate date = startDate.plusDays(i);
            Map<String, Double> dayStats = new HashMap<>();
            dayStats.put("calories", 0.0);
            dayStats.put("protein", 0.0);
            dayStats.put("fat", 0.0);
            dayStats.put("carbs", 0.0);
            dailyData.put(date, dayStats);
        }

        for (NutritionRecord record : records) {
            LocalDate date = record.getDate();
            if (dailyData.containsKey(date)) {
                Map<String, Double> dayStats = dailyData.get(date);
                dayStats.put("calories", dayStats.get("calories") + (record.getCalories() != null ? record.getCalories() : 0));
                dayStats.put("protein", dayStats.get("protein") + (record.getProtein() != null ? record.getProtein() : 0));
                dayStats.put("fat", dayStats.get("fat") + (record.getFat() != null ? record.getFat() : 0));
                dayStats.put("carbs", dayStats.get("carbs") + (record.getCarbohydrates() != null ? record.getCarbohydrates() : 0));
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<LocalDate, Map<String, Double>> entry : dailyData.entrySet()) {
            Map<String, Object> dayMap = new HashMap<>();
            dayMap.put("date", entry.getKey().toString());
            dayMap.put("calories", Math.round(entry.getValue().get("calories")));
            dayMap.put("protein", roundToOneDecimal(entry.getValue().get("protein")));
            dayMap.put("fat", roundToOneDecimal(entry.getValue().get("fat")));
            dayMap.put("carbs", roundToOneDecimal(entry.getValue().get("carbs")));
            result.add(dayMap);
        }

        return result;
    }

    /**
     * ユーザーIDのバリデーション
     */
    private void validateUserId(String userId) {
        if (userId == null || userId.isEmpty()) {
            throw new ValidationException("ユーザーIDは必須です");
        }
    }

    /**
     * 小数点1位で四捨五入
     * @param value 丸める値
     * @return 小数点1位まで丸めた値
     */
    private double roundToOneDecimal(double value) {
        return Math.round(value * NutritionDefaults.DECIMAL_PRECISION) / NutritionDefaults.DECIMAL_PRECISION;
    }
}
