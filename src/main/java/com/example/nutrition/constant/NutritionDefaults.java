package com.example.nutrition.constant;

/**
 * 栄養管理アプリのデフォルト値定数
 */
public final class NutritionDefaults {

    private NutritionDefaults() {
        // インスタンス化防止
    }

    // ========================================
    // 新規ユーザーのデフォルト目標値
    // ========================================

    /** デフォルト目標カロリー (kcal) */
    public static final int DEFAULT_TARGET_CALORIES = 2000;

    /** デフォルト目標タンパク質 (g) */
    public static final int DEFAULT_TARGET_PROTEIN = 60;

    /** デフォルト目標脂質 (g) */
    public static final int DEFAULT_TARGET_FAT = 65;

    /** デフォルト目標炭水化物 (g) */
    public static final int DEFAULT_TARGET_CARBS = 250;

    /** デフォルトユーザー名 */
    public static final String DEFAULT_USER_NAME = "ユーザー";

    // ========================================
    // 表示用の丸め精度
    // ========================================

    /** 小数点1位までの丸め乗数 */
    public static final double DECIMAL_PRECISION = 10.0;

    // ========================================
    // 週間統計の日数
    // ========================================

    /** 週間統計の日数 */
    public static final int WEEKLY_STATS_DAYS = 7;
}
