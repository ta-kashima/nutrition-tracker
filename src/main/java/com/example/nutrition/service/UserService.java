package com.example.nutrition.service;

import com.example.nutrition.constant.NutritionDefaults;
import com.example.nutrition.exception.ResourceNotFoundException;
import com.example.nutrition.exception.ValidationException;
import com.example.nutrition.model.User;
import com.example.nutrition.repository.UserRepository;
import com.example.nutrition.security.LoginUser;
import com.example.nutrition.controller.AuthController;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.Optional;

/**
 * ユーザーサービス
 * ユーザー情報の取得・作成・更新を担当
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * セッションから現在のログインユーザーを取得
     * @return ログイン中のユーザー、未ログインの場合はnull
     */
    @Transactional(readOnly = true)
    public User getCurrentUser() {
        LoginUser loginUser = getLoginUserFromSession();
        if (loginUser != null) {
            return userRepository.findById(loginUser.getUserId())
                    .orElse(null);
        }
        return null;
    }

    /**
     * 現在のリクエストコンテキストからログインユーザー情報を取得
     *
     * RequestContextHolderを使用することで、Spring Servlet環境でのセッション
     * アクセスを実現している。WebFluxや非Servlet環境では利用不可。
     *
     * @return ログインユーザー情報、セッション未初期化時はnull
     */
    private LoginUser getLoginUserFromSession() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpSession session = attrs.getRequest().getSession(false);
            if (session != null) {
                return (LoginUser) session.getAttribute(AuthController.SESSION_LOGIN_USER);
            }
        }
        return null;
    }

    /**
     * ユーザーIDで検索
     */
    @Transactional(readOnly = true)
    public Optional<User> findByUserId(String userId) {
        return userRepository.findById(userId);
    }

    /**
     * メールアドレスで検索
     */
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * 新規ユーザー作成
     * デフォルトの栄養目標値を設定して保存
     */
    @Transactional
    public User createUser(String userId, String email, String name) {
        User user = User.builder()
                .userId(userId)
                .email(email)
                .name(name != null ? name : NutritionDefaults.DEFAULT_USER_NAME)
                .targetCalories(NutritionDefaults.DEFAULT_TARGET_CALORIES)
                .targetProtein(NutritionDefaults.DEFAULT_TARGET_PROTEIN)
                .targetFat(NutritionDefaults.DEFAULT_TARGET_FAT)
                .targetCarbs(NutritionDefaults.DEFAULT_TARGET_CARBS)
                .build();
        return userRepository.save(user);
    }

    /**
     * 最終ログイン日時を更新
     * @param userId ユーザーID（必須）
     */
    @Transactional
    public void updateLastLogin(String userId) {
        if (userId == null || userId.isEmpty()) {
            throw new ValidationException("ユーザーIDは必須です");
        }
        userRepository.findById(userId).ifPresent(user -> {
            user.setLastLoginAt(Instant.now());
            // JPA管理下のエンティティは自動的に更新されるため、save()は省略可能
        });
    }

    /**
     * 現在ログイン中のユーザー情報を更新
     */
    @Transactional
    public User updateUser(User user) {
        User existing = getCurrentUser();
        if (existing == null) {
            throw new ValidationException("ログインが必要です");
        }

        updateUserFields(existing, user);
        return userRepository.save(existing);
    }

    /**
     * ユーザーIDで指定したユーザー情報を更新
     */
    @Transactional
    public User updateUserById(String userId, User user) {
        User existing = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("ユーザー", userId));

        updateUserFields(existing, user);
        return userRepository.save(existing);
    }

    /**
     * ユーザーフィールドを更新（共通処理）
     * nullでないフィールドのみ更新する
     */
    private void updateUserFields(User existing, User newData) {
        if (newData.getName() != null) existing.setName(newData.getName());
        if (newData.getAge() != null) existing.setAge(newData.getAge());
        if (newData.getGender() != null) existing.setGender(newData.getGender());
        if (newData.getHeight() != null) existing.setHeight(newData.getHeight());
        if (newData.getWeight() != null) existing.setWeight(newData.getWeight());
        if (newData.getActivityLevel() != null) existing.setActivityLevel(newData.getActivityLevel());
        if (newData.getTargetCalories() != null) existing.setTargetCalories(newData.getTargetCalories());
        if (newData.getTargetProtein() != null) existing.setTargetProtein(newData.getTargetProtein());
        if (newData.getTargetFat() != null) existing.setTargetFat(newData.getTargetFat());
        if (newData.getTargetCarbs() != null) existing.setTargetCarbs(newData.getTargetCarbs());
    }
}
