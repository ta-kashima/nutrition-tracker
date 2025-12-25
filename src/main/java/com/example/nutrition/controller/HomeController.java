package com.example.nutrition.controller;

import com.example.nutrition.model.FoodMaster;
import com.example.nutrition.model.MealSet;
import com.example.nutrition.model.MealSetFood;
import com.example.nutrition.model.NutritionRecord;
import com.example.nutrition.model.User;
import com.example.nutrition.service.FoodMasterService;
import com.example.nutrition.service.MealSetService;
import com.example.nutrition.service.NutritionRecordService;
import com.example.nutrition.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 画面遷移コントローラー
 */
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final UserService userService;
    private final FoodMasterService foodMasterService;
    private final NutritionRecordService recordService;
    private final MealSetService mealSetService;

    /**
     * ホーム画面
     */
    @GetMapping("/")
    public String index(Model model) {
        User user = userService.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        LocalDate today = LocalDate.now();
        Map<String, Object> summary = recordService.getDailySummaryByUserId(user.getUserId(), today);

        model.addAttribute("summary", summary);
        model.addAttribute("user", user);
        model.addAttribute("today", today);
        return "index";
    }

    /**
     * ダッシュボード
     */
    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {

        User user = userService.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        if (date == null) {
            date = LocalDate.now();
        }

        Map<String, Object> summary = recordService.getDailySummaryByUserId(user.getUserId(), date);
        List<Map<String, Object>> weeklyStats = recordService.getWeeklyStatisticsByUserId(user.getUserId(), date);

        model.addAttribute("summary", summary);
        model.addAttribute("user", user);
        model.addAttribute("selectedDate", date);
        model.addAttribute("weeklyStats", weeklyStats);
        return "dashboard";
    }

    /**
     * 記録一覧・登録画面
     */
    @GetMapping("/records")
    public String records(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String mealType,
            Model model) {

        User user = userService.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        if (date == null) {
            date = LocalDate.now();
        }

        List<NutritionRecord> records = recordService.getRecordsByUserIdAndDateAndMealType(user.getUserId(), date, mealType);
        List<FoodMaster> foods = foodMasterService.getFoodsForUser(user.getUserId());
        Map<String, Object> summary = recordService.getDailySummaryByUserId(user.getUserId(), date);

        model.addAttribute("records", records);
        model.addAttribute("foods", foods);
        model.addAttribute("summary", summary);
        model.addAttribute("selectedDate", date);
        model.addAttribute("selectedMealType", mealType);
        model.addAttribute("user", user);
        model.addAttribute("newRecord", new NutritionRecord());
        return "records";
    }

    /**
     * 記録登録処理
     */
    @PostMapping("/records")
    public String createRecord(
            @ModelAttribute NutritionRecord record,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        User user = userService.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        record.setDate(date);
        record.setUserId(user.getUserId());
        recordService.createRecord(record);
        return "redirect:/records?date=" + date;
    }

    /**
     * 記録削除処理
     */
    @PostMapping("/records/{id}/delete")
    public String deleteRecord(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        User user = userService.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        recordService.deleteRecord(id, user.getUserId());
        return "redirect:/records?date=" + date;
    }

    /**
     * 食品マスタ画面
     */
    @GetMapping("/foods")
    public String foods(
            @RequestParam(required = false) String keyword,
            Model model) {

        User user = userService.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        List<FoodMaster> foods;
        if (keyword != null && !keyword.isEmpty()) {
            foods = foodMasterService.searchFoodsForUser(user.getUserId(), keyword);
        } else {
            foods = foodMasterService.getFoodsForUser(user.getUserId());
        }

        model.addAttribute("foods", foods);
        model.addAttribute("keyword", keyword);
        model.addAttribute("user", user);
        model.addAttribute("newFood", new FoodMaster());
        return "foods";
    }

    /**
     * 食品登録処理
     */
    @PostMapping("/foods")
    public String createFood(@ModelAttribute FoodMaster food) {
        User user = userService.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        food.setIsPublic(false);
        food.setUserId(user.getUserId());
        foodMasterService.createFood(food);
        return "redirect:/foods";
    }

    /**
     * 食品削除処理
     */
    @PostMapping("/foods/{id}/delete")
    public String deleteFood(@PathVariable Long id) {
        User user = userService.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        foodMasterService.deleteFood(id, user.getUserId());
        return "redirect:/foods";
    }

    /**
     * 設定画面
     */
    @GetMapping("/settings")
    public String settings(Model model) {
        User user = userService.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "settings";
    }

    /**
     * 設定更新処理
     */
    @PostMapping("/settings")
    public String updateSettings(@ModelAttribute User user, Model model) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        userService.updateUser(user);
        model.addAttribute("user", userService.getCurrentUser());
        model.addAttribute("message", "設定を保存しました");
        return "settings";
    }

    // ========================================
    // 食事セット関連
    // ========================================

    /**
     * 食事セット一覧画面
     */
    @GetMapping("/meal-sets")
    public String mealSets(
            @RequestParam(required = false) String mealType,
            @RequestParam(required = false) String keyword,
            Model model) {

        User user = userService.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        List<MealSet> mealSets;
        if (keyword != null && !keyword.isEmpty()) {
            mealSets = mealSetService.searchMealSets(user.getUserId(), keyword);
        } else {
            mealSets = mealSetService.getMealSetsByUserIdAndMealType(user.getUserId(), mealType);
        }

        List<FoodMaster> foods = foodMasterService.getFoodsForUser(user.getUserId());

        model.addAttribute("mealSets", mealSets);
        model.addAttribute("foods", foods);
        model.addAttribute("selectedMealType", mealType);
        model.addAttribute("keyword", keyword);
        model.addAttribute("user", user);
        model.addAttribute("newMealSet", new MealSet());
        return "meal-sets";
    }

    /**
     * 食事セット登録処理
     */
    @PostMapping("/meal-sets")
    public String createMealSet(
            @RequestParam String setName,
            @RequestParam(required = false) String mealType,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) List<Long> foodIds,
            @RequestParam(required = false) List<String> foodNames,
            @RequestParam(required = false) List<Double> gramsList,
            @RequestParam(required = false) List<Double> caloriesList,
            @RequestParam(required = false) List<Double> proteinList,
            @RequestParam(required = false) List<Double> fatList,
            @RequestParam(required = false) List<Double> carbsList) {

        User user = userService.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        MealSet mealSet = MealSet.builder()
                .userId(user.getUserId())
                .setName(setName)
                .mealType(mealType)
                .description(description)
                .build();

        // 食品リストを構築
        if (foodNames != null) {
            for (int i = 0; i < foodNames.size(); i++) {
                MealSetFood food = MealSetFood.builder()
                        .foodId(foodIds != null && i < foodIds.size() ? foodIds.get(i) : null)
                        .foodName(foodNames.get(i))
                        .grams(gramsList != null && i < gramsList.size() ? gramsList.get(i) : null)
                        .calories(caloriesList != null && i < caloriesList.size() ? caloriesList.get(i) : null)
                        .protein(proteinList != null && i < proteinList.size() ? proteinList.get(i) : null)
                        .fat(fatList != null && i < fatList.size() ? fatList.get(i) : null)
                        .carbohydrates(carbsList != null && i < carbsList.size() ? carbsList.get(i) : null)
                        .sortOrder(i)
                        .build();
                mealSet.getFoods().add(food);
            }
        }

        mealSetService.createMealSet(mealSet);
        return "redirect:/meal-sets";
    }

    /**
     * 食事セットを使用して記録を作成
     */
    @PostMapping("/meal-sets/{id}/use")
    public String useMealSet(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String mealType) {

        User user = userService.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        mealSetService.useMealSet(id, user.getUserId(), date, mealType);
        return "redirect:/records?date=" + date;
    }

    /**
     * 食事セット削除処理
     */
    @PostMapping("/meal-sets/{id}/delete")
    public String deleteMealSet(@PathVariable Long id) {
        User user = userService.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        mealSetService.deleteMealSet(id, user.getUserId());
        return "redirect:/meal-sets";
    }
}
