package com.example.nutrition.service;

import com.example.nutrition.exception.AccessDeniedException;
import com.example.nutrition.exception.ResourceNotFoundException;
import com.example.nutrition.exception.ValidationException;
import com.example.nutrition.model.MealSet;
import com.example.nutrition.model.MealSetFood;
import com.example.nutrition.model.NutritionRecord;
import com.example.nutrition.repository.MealSetRepository;
import com.example.nutrition.repository.NutritionRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * 食事セットサービス
 * 食事セットの作成・取得・更新・削除・使用を担当
 */
@Service
@RequiredArgsConstructor
public class MealSetService {

    private final MealSetRepository mealSetRepository;
    private final NutritionRecordRepository nutritionRecordRepository;

    /**
     * ユーザーの食事セット一覧を取得
     */
    @Transactional(readOnly = true)
    public List<MealSet> getMealSetsByUserId(String userId) {
        validateUserId(userId);
        return mealSetRepository.findByUserIdOrderByUsageCountDesc(userId);
    }

    /**
     * 食事タイプで絞り込み
     */
    @Transactional(readOnly = true)
    public List<MealSet> getMealSetsByUserIdAndMealType(String userId, String mealType) {
        if (mealType == null || mealType.isEmpty() || "all".equals(mealType)) {
            return getMealSetsByUserId(userId);
        }
        validateUserId(userId);
        return mealSetRepository.findByUserIdAndMealTypeOrderByUsageCountDesc(userId, mealType);
    }

    /**
     * 食事セットを検索
     */
    @Transactional(readOnly = true)
    public List<MealSet> searchMealSets(String userId, String keyword) {
        validateUserId(userId);
        if (keyword == null || keyword.isEmpty()) {
            return getMealSetsByUserId(userId);
        }
        return mealSetRepository.findByUserIdAndSetNameContainingIgnoreCase(userId, keyword);
    }

    /**
     * 食事セットをIDで取得（N+1対策: 食品リストを一括取得）
     */
    @Transactional(readOnly = true)
    public MealSet getMealSetById(Long id) {
        Objects.requireNonNull(id, "食事セットIDは必須です");
        return mealSetRepository.findByIdWithFoods(id)
                .orElseThrow(() -> new ResourceNotFoundException("食事セット", id));
    }

    /**
     * 食事セットを作成
     */
    @Transactional
    public MealSet createMealSet(MealSet mealSet) {
        Objects.requireNonNull(mealSet, "食事セットデータは必須です");
        validateUserId(mealSet.getUserId());

        // 食品リストの参照を設定
        if (mealSet.getFoods() != null) {
            for (MealSetFood food : mealSet.getFoods()) {
                food.setMealSet(mealSet);
            }
        }

        // 栄養素合計を計算
        mealSet.recalculateTotals();

        return mealSetRepository.save(mealSet);
    }

    /**
     * 食事セットを更新（所有者確認付き）
     */
    @Transactional
    public MealSet updateMealSet(Long id, MealSet mealSet, String userId) {
        Objects.requireNonNull(id, "食事セットIDは必須です");
        validateUserId(userId);

        MealSet existing = getMealSetById(id);

        // 所有者確認
        checkOwnership(existing.getUserId(), userId, "食事セット", "編集");

        existing.setSetName(mealSet.getSetName());
        existing.setMealType(mealSet.getMealType());
        existing.setDescription(mealSet.getDescription());

        // 食品リストを更新
        if (existing.getFoods() != null) {
            existing.getFoods().clear();
        }
        if (mealSet.getFoods() != null) {
            for (MealSetFood food : mealSet.getFoods()) {
                food.setMealSet(existing);
                existing.getFoods().add(food);
            }
        }

        // 栄養素合計を再計算
        existing.recalculateTotals();

        return mealSetRepository.save(existing);
    }

    /**
     * 食事セットを削除（所有者確認付き）
     */
    @Transactional
    public void deleteMealSet(Long id, String userId) {
        Objects.requireNonNull(id, "食事セットIDは必須です");
        validateUserId(userId);

        MealSet mealSet = getMealSetById(id);

        // 所有者確認
        checkOwnership(mealSet.getUserId(), userId, "食事セット", "削除");

        mealSetRepository.deleteById(id);
    }

    /**
     * 食事セットを使用して栄養記録を作成
     */
    @Transactional
    public List<NutritionRecord> useMealSet(Long setId, String userId, LocalDate date, String mealType) {
        Objects.requireNonNull(setId, "食事セットIDは必須です");
        validateUserId(userId);
        Objects.requireNonNull(date, "日付は必須です");

        MealSet mealSet = getMealSetById(setId);

        // 所有者確認
        checkOwnership(mealSet.getUserId(), userId, "食事セット", "使用");

        // 使用回数をインクリメント
        int currentCount = mealSet.getUsageCount() != null ? mealSet.getUsageCount() : 0;
        mealSet.setUsageCount(currentCount + 1);
        mealSetRepository.save(mealSet);

        // 食事タイプを決定（指定がなければセットのデフォルト値）
        String recordMealType = (mealType != null && !mealType.isEmpty()) ? mealType : mealSet.getMealType();

        // セット内の各食品を栄養記録として登録
        List<NutritionRecord> records = mealSet.getFoods().stream()
                .map(food -> NutritionRecord.builder()
                        .userId(userId)
                        .date(date)
                        .mealType(recordMealType)
                        .foodId(food.getFoodId())
                        .foodName(food.getFoodName())
                        .grams(food.getGrams())
                        .calories(food.getCalories())
                        .protein(food.getProtein())
                        .fat(food.getFat())
                        .carbohydrates(food.getCarbohydrates())
                        .build())
                .toList();

        return nutritionRecordRepository.saveAll(records);
    }

    /**
     * 食事セットに食品を追加
     */
    @Transactional
    public MealSet addFoodToMealSet(Long setId, MealSetFood food, String userId) {
        Objects.requireNonNull(setId, "食事セットIDは必須です");
        Objects.requireNonNull(food, "食品データは必須です");
        validateUserId(userId);

        MealSet mealSet = getMealSetById(setId);

        // 所有者確認
        checkOwnership(mealSet.getUserId(), userId, "食事セット", "編集");

        food.setMealSet(mealSet);
        int sortOrder = mealSet.getFoods() != null ? mealSet.getFoods().size() : 0;
        food.setSortOrder(sortOrder);
        if (mealSet.getFoods() != null) {
            mealSet.getFoods().add(food);
        }
        mealSet.recalculateTotals();

        return mealSetRepository.save(mealSet);
    }

    /**
     * 食事セットから食品を削除
     */
    @Transactional
    public MealSet removeFoodFromMealSet(Long setId, Long foodId, String userId) {
        Objects.requireNonNull(setId, "食事セットIDは必須です");
        Objects.requireNonNull(foodId, "食品IDは必須です");
        validateUserId(userId);

        MealSet mealSet = getMealSetById(setId);

        // 所有者確認
        checkOwnership(mealSet.getUserId(), userId, "食事セット", "編集");

        if (mealSet.getFoods() != null) {
            mealSet.getFoods().removeIf(f -> Objects.equals(f.getId(), foodId));
        }
        mealSet.recalculateTotals();

        return mealSetRepository.save(mealSet);
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
     * 所有者確認（共通処理）
     */
    private void checkOwnership(String resourceOwnerId, String currentUserId,
            String resourceType, String action) {
        if (!Objects.equals(resourceOwnerId, currentUserId)) {
            throw new AccessDeniedException(resourceType, action);
        }
    }
}
