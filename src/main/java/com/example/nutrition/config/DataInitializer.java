package com.example.nutrition.config;

import com.example.nutrition.model.FoodMaster;
import com.example.nutrition.repository.FoodMasterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * 初期データ投入クラス
 * 公開食品マスタのみを初期化（ユーザーはFirebase認証で作成）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final FoodMasterRepository foodMasterRepository;

    @Override
    @Transactional
    public void run(String... args) {
        // 初期食品データの投入を無効化（ユーザー登録食品のみ使用）
        // createInitialFoods();

        // 既存の公開食品（デフォルト食品）を削除
        long publicCount = foodMasterRepository.countByIsPublicTrue();
        if (publicCount > 0) {
            foodMasterRepository.deleteByIsPublicTrue();
            log.info("{}件の公開食品データを削除しました", publicCount);
        }
    }

    @Transactional
    public void createInitialFoods() {
        if (foodMasterRepository.count() > 0) {
            log.info("食品データは既に存在します");
            return;
        }

        log.info("初期食品データを投入します...");

        List<FoodMaster> foods = Arrays.asList(
                // ========== 主食 ==========
                createFood("白米（1杯）", "breakfast", 150.0, 252.0, 3.8, 0.5, 55.7),
                createFood("玄米（1杯）", "breakfast", 150.0, 248.0, 4.2, 1.5, 53.4),
                createFood("もち麦ごはん（1杯）", "breakfast", 150.0, 240.0, 4.0, 1.0, 52.0),
                createFood("食パン（6枚切り1枚）", "breakfast", 60.0, 158.0, 5.6, 2.6, 28.0),
                createFood("食パン（8枚切り1枚）", "breakfast", 45.0, 119.0, 4.2, 2.0, 21.0),
                createFood("ロールパン（1個）", "breakfast", 30.0, 95.0, 3.0, 2.7, 14.6),
                createFood("クロワッサン（1個）", "breakfast", 40.0, 179.0, 3.2, 10.7, 17.0),
                createFood("フランスパン（1切れ）", "breakfast", 30.0, 84.0, 2.8, 0.4, 16.5),
                createFood("うどん（1玉）", "lunch", 200.0, 210.0, 5.2, 0.8, 43.2),
                createFood("そば（1玉）", "lunch", 170.0, 224.0, 8.2, 1.5, 43.2),
                createFood("ラーメン（生麺）", "lunch", 120.0, 337.0, 9.0, 1.3, 69.0),
                createFood("パスタ（乾麺100g）", "dinner", 100.0, 378.0, 13.0, 2.2, 73.9),
                createFood("焼きそば（1玉）", "lunch", 150.0, 224.0, 5.0, 2.0, 44.0),
                createFood("おにぎり（1個）", "snack", 100.0, 179.0, 2.7, 0.3, 39.4),
                createFood("お粥（1杯）", "breakfast", 250.0, 89.0, 1.6, 0.2, 19.4),
                createFood("オートミール（30g）", "breakfast", 30.0, 114.0, 4.1, 1.7, 20.7),
                createFood("シリアル（40g）", "breakfast", 40.0, 152.0, 2.8, 0.6, 33.2),
                createFood("グラノーラ（50g）", "breakfast", 50.0, 223.0, 4.0, 7.8, 35.5),

                // ========== 肉類 ==========
                createFood("鶏むね肉（100g）", "dinner", 100.0, 108.0, 22.3, 1.5, 0.0),
                createFood("鶏もも肉（100g）", "dinner", 100.0, 200.0, 16.2, 14.0, 0.0),
                createFood("鶏ささみ（100g）", "dinner", 100.0, 105.0, 23.0, 0.8, 0.0),
                createFood("鶏手羽先（2本）", "dinner", 60.0, 132.0, 10.6, 9.6, 0.0),
                createFood("豚ロース（100g）", "dinner", 100.0, 263.0, 19.3, 19.2, 0.2),
                createFood("豚バラ（100g）", "dinner", 100.0, 386.0, 14.2, 34.6, 0.1),
                createFood("豚ヒレ（100g）", "dinner", 100.0, 115.0, 22.8, 1.9, 0.2),
                createFood("豚ひき肉（100g）", "dinner", 100.0, 221.0, 18.6, 15.1, 0.0),
                createFood("牛もも肉（100g）", "dinner", 100.0, 182.0, 21.2, 9.6, 0.5),
                createFood("牛バラ（100g）", "dinner", 100.0, 371.0, 14.4, 32.9, 0.2),
                createFood("牛ひき肉（100g）", "dinner", 100.0, 224.0, 19.0, 15.1, 0.3),
                createFood("ベーコン（2枚）", "breakfast", 34.0, 138.0, 4.4, 13.2, 0.1),
                createFood("ハム（2枚）", "breakfast", 20.0, 39.0, 3.3, 2.8, 0.3),
                createFood("ソーセージ（2本）", "breakfast", 40.0, 128.0, 4.6, 11.5, 1.2),
                createFood("焼き鳥（2本）", "dinner", 60.0, 108.0, 13.2, 5.4, 1.2),

                // ========== 魚介類 ==========
                createFood("鮭（1切れ）", "dinner", 80.0, 110.0, 17.8, 3.5, 0.1),
                createFood("サバ（1切れ）", "dinner", 80.0, 166.0, 16.5, 10.0, 0.2),
                createFood("アジ（1尾）", "dinner", 60.0, 76.0, 12.4, 2.4, 0.1),
                createFood("マグロ赤身（刺身5切）", "dinner", 50.0, 63.0, 13.2, 0.7, 0.1),
                createFood("サーモン（刺身5切）", "dinner", 50.0, 104.0, 10.3, 6.5, 0.2),
                createFood("エビ（5尾）", "dinner", 50.0, 49.0, 10.9, 0.3, 0.1),
                createFood("イカ（100g）", "dinner", 100.0, 88.0, 18.1, 1.2, 0.2),
                createFood("タコ（100g）", "dinner", 100.0, 76.0, 16.4, 0.7, 0.1),
                createFood("ツナ缶（1缶）", "lunch", 70.0, 97.0, 12.8, 4.8, 0.1),
                createFood("サバ缶（1缶）", "lunch", 100.0, 190.0, 20.9, 10.7, 0.2),
                createFood("しらす（大さじ2）", "breakfast", 20.0, 25.0, 4.8, 0.4, 0.1),
                createFood("ちくわ（1本）", "snack", 30.0, 36.0, 3.7, 0.6, 4.1),
                createFood("かまぼこ（2切れ）", "dinner", 30.0, 29.0, 3.6, 0.3, 2.9),

                // ========== 卵・大豆製品 ==========
                createFood("卵（1個）", "breakfast", 60.0, 91.0, 7.4, 6.2, 0.2),
                createFood("卵焼き（2切れ）", "breakfast", 60.0, 97.0, 6.2, 7.0, 2.0),
                createFood("目玉焼き", "breakfast", 60.0, 105.0, 7.4, 8.0, 0.3),
                createFood("スクランブルエッグ", "breakfast", 80.0, 140.0, 9.0, 11.0, 1.0),
                createFood("ゆで卵（1個）", "snack", 60.0, 91.0, 7.7, 6.0, 0.2),
                createFood("納豆（1パック）", "breakfast", 45.0, 90.0, 7.4, 4.5, 5.4),
                createFood("豆腐（半丁）", "dinner", 150.0, 84.0, 7.4, 4.2, 2.4),
                createFood("豆腐（1丁）", "dinner", 300.0, 168.0, 14.8, 8.4, 4.8),
                createFood("厚揚げ（1枚）", "dinner", 100.0, 150.0, 10.7, 11.3, 0.9),
                createFood("油揚げ（1枚）", "dinner", 20.0, 77.0, 3.7, 6.6, 0.3),
                createFood("枝豆（100g）", "snack", 100.0, 135.0, 11.7, 6.2, 8.8),
                createFood("豆乳（200ml）", "breakfast", 200.0, 92.0, 7.2, 4.0, 6.2),

                // ========== 野菜 ==========
                createFood("サラダ（1皿）", "lunch", 100.0, 20.0, 1.0, 0.2, 4.0),
                createFood("キャベツ（100g）", "dinner", 100.0, 23.0, 1.3, 0.2, 5.2),
                createFood("レタス（100g）", "lunch", 100.0, 12.0, 0.6, 0.1, 2.8),
                createFood("トマト（1個）", "lunch", 150.0, 29.0, 1.4, 0.2, 7.0),
                createFood("きゅうり（1本）", "lunch", 100.0, 14.0, 1.0, 0.1, 3.0),
                createFood("にんじん（1本）", "dinner", 150.0, 56.0, 1.1, 0.2, 13.5),
                createFood("たまねぎ（1個）", "dinner", 200.0, 74.0, 2.0, 0.2, 17.4),
                createFood("じゃがいも（1個）", "dinner", 150.0, 114.0, 2.4, 0.2, 26.3),
                createFood("さつまいも（1/2本）", "snack", 100.0, 132.0, 1.2, 0.2, 31.5),
                createFood("かぼちゃ（100g）", "dinner", 100.0, 49.0, 1.6, 0.1, 10.9),
                createFood("ブロッコリー（100g）", "dinner", 100.0, 33.0, 4.3, 0.5, 5.2),
                createFood("ほうれん草（1束）", "dinner", 100.0, 20.0, 2.2, 0.4, 3.1),
                createFood("小松菜（100g）", "dinner", 100.0, 14.0, 1.5, 0.2, 2.4),
                createFood("もやし（1袋）", "dinner", 200.0, 28.0, 3.4, 0.2, 5.2),
                createFood("なす（1本）", "dinner", 80.0, 18.0, 0.9, 0.1, 4.0),
                createFood("ピーマン（2個）", "dinner", 60.0, 13.0, 0.5, 0.1, 3.1),
                createFood("アボカド（1/2個）", "lunch", 70.0, 131.0, 1.8, 13.2, 4.3),
                createFood("大根（100g）", "dinner", 100.0, 18.0, 0.4, 0.1, 4.1),
                createFood("白菜（100g）", "dinner", 100.0, 14.0, 0.8, 0.1, 3.2),
                createFood("ごぼう（100g）", "dinner", 100.0, 65.0, 1.8, 0.1, 15.4),
                createFood("れんこん（100g）", "dinner", 100.0, 66.0, 1.9, 0.1, 15.5),
                createFood("アスパラガス（3本）", "dinner", 60.0, 13.0, 1.6, 0.1, 2.3),
                createFood("きのこミックス（100g）", "dinner", 100.0, 18.0, 2.7, 0.4, 4.8),
                createFood("しめじ（100g）", "dinner", 100.0, 18.0, 2.7, 0.4, 4.8),
                createFood("えのき（100g）", "dinner", 100.0, 22.0, 2.7, 0.2, 7.4),
                createFood("わかめ（10g）", "lunch", 10.0, 2.0, 0.2, 0.0, 0.4),

                // ========== 果物 ==========
                createFood("バナナ（1本）", "snack", 100.0, 86.0, 1.1, 0.2, 22.5),
                createFood("りんご（1個）", "snack", 200.0, 108.0, 0.4, 0.2, 29.2),
                createFood("みかん（1個）", "snack", 80.0, 37.0, 0.6, 0.1, 9.0),
                createFood("オレンジ（1個）", "snack", 130.0, 57.0, 1.2, 0.1, 14.3),
                createFood("グレープフルーツ（1/2個）", "breakfast", 120.0, 46.0, 1.1, 0.1, 11.4),
                createFood("いちご（5個）", "snack", 75.0, 26.0, 0.7, 0.1, 6.4),
                createFood("ぶどう（10粒）", "snack", 50.0, 30.0, 0.2, 0.1, 7.8),
                createFood("キウイ（1個）", "snack", 85.0, 46.0, 0.9, 0.1, 11.6),
                createFood("梨（1/2個）", "snack", 150.0, 65.0, 0.5, 0.2, 16.5),
                createFood("桃（1個）", "snack", 170.0, 68.0, 1.0, 0.2, 15.6),
                createFood("スイカ（1切れ）", "snack", 200.0, 74.0, 1.2, 0.2, 18.4),
                createFood("メロン（1/8個）", "snack", 100.0, 42.0, 1.0, 0.1, 10.4),
                createFood("パイナップル（100g）", "snack", 100.0, 51.0, 0.6, 0.1, 13.4),
                createFood("マンゴー（1/2個）", "snack", 100.0, 64.0, 0.6, 0.1, 16.9),
                createFood("ブルーベリー（50g）", "snack", 50.0, 25.0, 0.3, 0.1, 6.3),

                // ========== 乳製品 ==========
                createFood("牛乳（200ml）", "breakfast", 200.0, 134.0, 6.6, 7.6, 9.6),
                createFood("低脂肪牛乳（200ml）", "breakfast", 200.0, 92.0, 7.6, 2.0, 11.0),
                createFood("ヨーグルト（100g）", "breakfast", 100.0, 62.0, 3.6, 3.0, 4.9),
                createFood("ギリシャヨーグルト（100g）", "breakfast", 100.0, 100.0, 10.0, 5.0, 4.0),
                createFood("チーズ（1枚）", "snack", 20.0, 68.0, 4.5, 5.2, 0.3),
                createFood("クリームチーズ（大さじ1）", "breakfast", 15.0, 52.0, 1.2, 5.2, 0.4),
                createFood("モッツァレラチーズ（50g）", "lunch", 50.0, 138.0, 9.2, 10.8, 0.5),
                createFood("カッテージチーズ（50g）", "breakfast", 50.0, 53.0, 6.7, 2.3, 1.0),

                // ========== 調味料・油 ==========
                createFood("オリーブオイル（大さじ1）", "dinner", 12.0, 111.0, 0.0, 12.0, 0.0),
                createFood("ごま油（大さじ1）", "dinner", 12.0, 111.0, 0.0, 12.0, 0.0),
                createFood("バター（10g）", "breakfast", 10.0, 75.0, 0.1, 8.1, 0.0),
                createFood("マヨネーズ（大さじ1）", "lunch", 12.0, 84.0, 0.2, 9.0, 0.4),
                createFood("ドレッシング（大さじ1）", "lunch", 15.0, 61.0, 0.2, 6.0, 1.8),
                createFood("ケチャップ（大さじ1）", "lunch", 15.0, 18.0, 0.3, 0.0, 4.0),
                createFood("味噌（大さじ1）", "breakfast", 18.0, 35.0, 2.3, 1.1, 3.8),

                // ========== 飲料 ==========
                createFood("コーヒー（ブラック）", "breakfast", 150.0, 6.0, 0.3, 0.0, 1.1),
                createFood("カフェラテ（1杯）", "breakfast", 200.0, 100.0, 5.0, 5.0, 8.0),
                createFood("緑茶", "snack", 150.0, 3.0, 0.3, 0.0, 0.3),
                createFood("紅茶（ストレート）", "snack", 150.0, 2.0, 0.2, 0.0, 0.2),
                createFood("オレンジジュース（200ml）", "breakfast", 200.0, 84.0, 1.4, 0.2, 21.0),
                createFood("野菜ジュース（200ml）", "breakfast", 200.0, 68.0, 1.6, 0.0, 15.2),
                createFood("スポーツドリンク（500ml）", "snack", 500.0, 105.0, 0.0, 0.0, 26.0),
                createFood("プロテインドリンク（1杯）", "snack", 200.0, 120.0, 24.0, 1.0, 3.0),
                createFood("スムージー（1杯）", "breakfast", 250.0, 150.0, 3.0, 1.0, 32.0),

                // ========== 定番料理 ==========
                createFood("味噌汁（1杯）", "breakfast", 150.0, 32.0, 2.1, 0.8, 4.2),
                createFood("豚汁（1杯）", "dinner", 200.0, 120.0, 6.0, 5.0, 12.0),
                createFood("カレーライス", "dinner", 400.0, 600.0, 15.0, 18.0, 90.0),
                createFood("ハヤシライス", "dinner", 400.0, 580.0, 14.0, 16.0, 88.0),
                createFood("親子丼", "lunch", 350.0, 550.0, 22.0, 12.0, 80.0),
                createFood("牛丼", "lunch", 350.0, 650.0, 20.0, 20.0, 88.0),
                createFood("天丼", "lunch", 350.0, 700.0, 15.0, 25.0, 95.0),
                createFood("チャーハン", "lunch", 300.0, 500.0, 12.0, 15.0, 72.0),
                createFood("オムライス", "lunch", 350.0, 600.0, 18.0, 22.0, 75.0),
                createFood("ハンバーグ（1個）", "dinner", 120.0, 268.0, 15.6, 18.0, 9.6),
                createFood("から揚げ（5個）", "dinner", 100.0, 290.0, 18.0, 20.0, 8.0),
                createFood("とんかつ（1枚）", "dinner", 120.0, 350.0, 20.0, 22.0, 15.0),
                createFood("餃子（6個）", "dinner", 90.0, 210.0, 8.0, 10.0, 20.0),
                createFood("焼き魚定食", "dinner", 350.0, 450.0, 28.0, 12.0, 55.0),
                createFood("生姜焼き定食", "dinner", 400.0, 650.0, 25.0, 22.0, 75.0),
                createFood("肉じゃが（1皿）", "dinner", 200.0, 220.0, 10.0, 8.0, 28.0),
                createFood("野菜炒め（1皿）", "dinner", 150.0, 120.0, 5.0, 8.0, 8.0),
                createFood("煮物（1皿）", "dinner", 150.0, 90.0, 4.0, 2.0, 14.0),
                createFood("冷奴", "dinner", 150.0, 84.0, 7.4, 4.2, 2.4),
                createFood("だし巻き卵", "breakfast", 80.0, 110.0, 8.0, 8.0, 2.0),

                // ========== 麺類料理 ==========
                createFood("ラーメン（醤油）", "lunch", 500.0, 500.0, 18.0, 12.0, 70.0),
                createFood("ラーメン（味噌）", "lunch", 500.0, 550.0, 20.0, 15.0, 72.0),
                createFood("ラーメン（とんこつ）", "lunch", 500.0, 600.0, 22.0, 20.0, 68.0),
                createFood("つけ麺", "lunch", 450.0, 650.0, 22.0, 18.0, 85.0),
                createFood("かけうどん", "lunch", 350.0, 280.0, 8.0, 1.0, 58.0),
                createFood("きつねうどん", "lunch", 400.0, 380.0, 12.0, 8.0, 60.0),
                createFood("天ぷらうどん", "lunch", 450.0, 480.0, 15.0, 15.0, 62.0),
                createFood("ざるそば", "lunch", 300.0, 300.0, 12.0, 2.0, 58.0),
                createFood("かけそば", "lunch", 350.0, 320.0, 12.0, 2.0, 60.0),
                createFood("ナポリタン", "lunch", 350.0, 550.0, 14.0, 18.0, 78.0),
                createFood("カルボナーラ", "dinner", 350.0, 680.0, 22.0, 32.0, 68.0),
                createFood("ミートソースパスタ", "dinner", 350.0, 600.0, 20.0, 18.0, 80.0),
                createFood("ペペロンチーノ", "dinner", 300.0, 450.0, 12.0, 15.0, 65.0),

                // ========== ファストフード・軽食 ==========
                createFood("ハンバーガー", "lunch", 150.0, 350.0, 15.0, 15.0, 35.0),
                createFood("チーズバーガー", "lunch", 170.0, 420.0, 20.0, 20.0, 35.0),
                createFood("フライドポテト（M）", "lunch", 120.0, 380.0, 4.0, 18.0, 48.0),
                createFood("ピザ（1切れ）", "dinner", 100.0, 270.0, 11.0, 12.0, 28.0),
                createFood("サンドイッチ（1個）", "lunch", 120.0, 250.0, 8.0, 10.0, 30.0),
                createFood("ホットドッグ", "lunch", 100.0, 280.0, 10.0, 15.0, 25.0),
                createFood("コロッケ（1個）", "lunch", 60.0, 140.0, 3.0, 8.0, 14.0),
                createFood("メンチカツ（1個）", "lunch", 80.0, 220.0, 10.0, 15.0, 12.0),

                // ========== お菓子・デザート ==========
                createFood("プロテインバー", "snack", 40.0, 160.0, 15.0, 6.0, 15.0),
                createFood("チョコレート（1枚）", "snack", 50.0, 279.0, 3.9, 17.4, 27.9),
                createFood("クッキー（3枚）", "snack", 30.0, 150.0, 2.0, 7.0, 20.0),
                createFood("ポテトチップス（小袋）", "snack", 30.0, 166.0, 1.5, 10.5, 16.2),
                createFood("アイスクリーム（1個）", "snack", 100.0, 180.0, 3.5, 8.0, 23.0),
                createFood("プリン（1個）", "snack", 100.0, 126.0, 5.5, 5.0, 14.0),
                createFood("ケーキ（1切れ）", "snack", 100.0, 340.0, 5.0, 18.0, 40.0),
                createFood("ドーナツ（1個）", "snack", 50.0, 190.0, 3.0, 10.0, 22.0),
                createFood("シュークリーム（1個）", "snack", 80.0, 200.0, 4.0, 12.0, 20.0),
                createFood("大福（1個）", "snack", 50.0, 120.0, 2.0, 0.2, 27.0),
                createFood("どら焼き（1個）", "snack", 70.0, 190.0, 4.0, 2.0, 40.0),
                createFood("せんべい（2枚）", "snack", 20.0, 76.0, 1.4, 0.4, 16.8),
                createFood("ナッツミックス（30g）", "snack", 30.0, 185.0, 5.4, 16.2, 5.7),
                createFood("アーモンド（20g）", "snack", 20.0, 122.0, 4.0, 10.8, 4.0),
                createFood("ドライフルーツ（30g）", "snack", 30.0, 90.0, 0.6, 0.1, 22.0)
        );

        foodMasterRepository.saveAll(foods);
        log.info("{}件の公開食品データを投入しました", foods.size());
    }

    private FoodMaster createFood(String name, String mealType, double grams,
                                   double calories, double protein, double fat, double carbs) {
        return FoodMaster.builder()
                .userId(null)  // 公開食品はuserIdなし
                .foodName(name)
                .mealType(mealType)
                .portionGrams(grams)
                .calories(calories)
                .protein(protein)
                .fat(fat)
                .carbohydrates(carbs)
                .isFavorite(false)
                .usageCount(0)
                .isPublic(true)  // 公開食品として登録
                .build();
    }
}
