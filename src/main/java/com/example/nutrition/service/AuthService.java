package com.example.nutrition.service;

import com.example.nutrition.config.FirebaseConfig;
import com.example.nutrition.model.User;
import com.example.nutrition.security.LoginUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Firebase認証サービス
 * Firebase認証の検証とユーザー管理を担当
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final FirebaseConfig firebaseConfig;
    private final UserService userService;

    /**
     * IDトークンを検証
     * @param idToken FirebaseのIDトークン
     * @return 検証済みトークン、Firebase無効時はnull
     */
    public FirebaseToken verifyIdToken(String idToken) throws FirebaseAuthException {
        log.debug("verifyIdToken called, firebaseEnabled={}", firebaseConfig.isFirebaseEnabled());

        FirebaseAuth auth = firebaseConfig.getFirebaseAuth();
        log.debug("FirebaseAuth instance: {}", auth != null ? "available" : "null");

        if (!firebaseConfig.isFirebaseEnabled()) {
            log.warn("Firebase is disabled. Token verification skipped.");
            return null;
        }

        if (auth == null) {
            log.error("FirebaseAuth is null - initialization may have failed");
            return null;
        }

        log.debug("Verifying token (length={})", idToken != null ? idToken.length() : 0);
        return auth.verifyIdToken(idToken);
    }

    /**
     * Firebase認証からユーザーを取得または作成
     */
    public User getOrCreateUser(FirebaseToken token) {
        String uid = token.getUid();
        String email = token.getEmail();
        String name = token.getName();

        return userService.findByUserId(uid)
                .orElseGet(() -> userService.createUser(uid, email, name));
    }

    /**
     * ログインユーザー情報を作成
     */
    public LoginUser createLoginUser(User user) {
        return LoginUser.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }

    /**
     * 開発モード用：デフォルトユーザーでログイン
     * Firebase無効時のみ使用可能
     */
    public LoginUser devModeLogin(String email) {
        if (firebaseConfig.isFirebaseEnabled()) {
            throw new IllegalStateException("開発モードログインはFirebase有効時は使用できません");
        }

        log.warn("開発モードログイン: {}", email);
        User user = userService.findByEmail(email)
                .orElseGet(() -> userService.createUser("dev-" + System.currentTimeMillis(), email, "開発ユーザー"));

        userService.updateLastLogin(user.getUserId());
        return createLoginUser(user);
    }

    /**
     * Firebase認証が有効かどうか
     */
    public boolean isFirebaseEnabled() {
        return firebaseConfig.isFirebaseEnabled();
    }
}
