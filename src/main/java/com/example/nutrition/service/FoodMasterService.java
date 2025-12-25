package com.example.nutrition.service;

import com.example.nutrition.exception.AccessDeniedException;
import com.example.nutrition.exception.ResourceNotFoundException;
import com.example.nutrition.exception.ValidationException;
import com.example.nutrition.model.FoodMaster;
import com.example.nutrition.repository.FoodMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 食品マスタサービス
 * 食品データの取得・登録・更新・削除を担当
 */
@Service
@RequiredArgsConstructor
public class FoodMasterService {

    private final FoodMasterRepository foodMasterRepository;

    @Transactional(readOnly = true)
    public List<FoodMaster> getAllFoods() {
        return foodMasterRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<FoodMaster> getPublicFoods() {
        return foodMasterRepository.findByIsPublicTrue();
    }

    @Transactional(readOnly = true)
    public FoodMaster getFoodById(Long id) {
        Objects.requireNonNull(id, "食品IDは必須です");
        return foodMasterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("食品", id));
    }

    @Transactional(readOnly = true)
    public List<FoodMaster> searchFoods(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return foodMasterRepository.findByIsPublicTrue();
        }
        return foodMasterRepository.findByFoodNameContainingIgnoreCase(keyword);
    }

    @Transactional(readOnly = true)
    public List<FoodMaster> getFoodsForUser(String userId) {
        if (userId == null || userId.isEmpty()) {
            throw new ValidationException("ユーザーIDは必須です");
        }
        return foodMasterRepository.findByUserIdOrIsPublicTrueOrderByFoodNameAsc(userId);
    }

    @Transactional(readOnly = true)
    public List<FoodMaster> searchFoodsForUser(String userId, String keyword) {
        if (userId == null || userId.isEmpty()) {
            throw new ValidationException("ユーザーIDは必須です");
        }
        if (keyword == null || keyword.isEmpty()) {
            return getFoodsForUser(userId);
        }
        return foodMasterRepository.findByUserIdOrIsPublicTrueAndFoodNameContainingIgnoreCase(userId, keyword);
    }

    @Transactional
    public FoodMaster createFood(FoodMaster food) {
        Objects.requireNonNull(food, "食品データは必須です");
        if (food.getUserId() == null || food.getUserId().isEmpty()) {
            throw new ValidationException("ユーザーIDは必須です");
        }
        return foodMasterRepository.save(food);
    }

    @Transactional
    public FoodMaster updateFood(Long id, FoodMaster food, String userId) {
        Objects.requireNonNull(id, "食品IDは必須です");
        Objects.requireNonNull(userId, "ユーザーIDは必須です");

        FoodMaster existing = getFoodById(id);

        // 公開食品は編集不可
        if (Boolean.TRUE.equals(existing.getIsPublic())) {
            throw new AccessDeniedException("公開食品", "編集");
        }

        // 所有者確認
        checkOwnership(existing.getUserId(), userId, "食品", "編集");

        existing.setFoodName(food.getFoodName());
        existing.setMealType(food.getMealType());
        existing.setPortionGrams(food.getPortionGrams());
        existing.setCalories(food.getCalories());
        existing.setProtein(food.getProtein());
        existing.setFat(food.getFat());
        existing.setCarbohydrates(food.getCarbohydrates());
        return foodMasterRepository.save(existing);
    }

    /**
     * 食品を削除（所有者確認付き）
     */
    @Transactional
    public void deleteFood(Long id, String userId) {
        Objects.requireNonNull(id, "食品IDは必須です");
        Objects.requireNonNull(userId, "ユーザーIDは必須です");

        FoodMaster food = getFoodById(id);

        // 公開食品は削除不可
        if (Boolean.TRUE.equals(food.getIsPublic())) {
            throw new AccessDeniedException("公開食品", "削除");
        }

        // 所有者確認
        checkOwnership(food.getUserId(), userId, "食品", "削除");

        foodMasterRepository.deleteById(id);
    }

    @Transactional
    public void incrementUsageCount(Long id) {
        Objects.requireNonNull(id, "食品IDは必須です");
        FoodMaster food = getFoodById(id);
        int currentCount = food.getUsageCount() != null ? food.getUsageCount() : 0;
        food.setUsageCount(currentCount + 1);
        foodMasterRepository.save(food);
    }

    @Transactional
    public FoodMaster toggleFavorite(Long id, String userId) {
        Objects.requireNonNull(id, "食品IDは必須です");
        Objects.requireNonNull(userId, "ユーザーIDは必須です");

        FoodMaster food = getFoodById(id);

        // 公開食品は誰でもお気に入り可能、非公開食品は所有者のみ
        if (!Boolean.TRUE.equals(food.getIsPublic())) {
            checkOwnership(food.getUserId(), userId, "食品のお気に入り", "変更");
        }

        food.setIsFavorite(!Boolean.TRUE.equals(food.getIsFavorite()));
        return foodMasterRepository.save(food);
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
