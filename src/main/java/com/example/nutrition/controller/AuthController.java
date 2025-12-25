package com.example.nutrition.controller;

import com.example.nutrition.model.User;
import com.example.nutrition.security.LoginUser;
import com.example.nutrition.service.AuthService;
import com.example.nutrition.service.UserService;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 認証コントローラー
 * ログイン・ログアウト・ユーザー情報取得を担当
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @Value("${firebase.enabled:true}")
    private boolean firebaseEnabled;

    public static final String SESSION_LOGIN_USER = "loginUser";

    /**
     * ログイン画面
     */
    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("firebaseEnabled", firebaseEnabled);
        return "login";
    }

    /**
     * 新規登録画面
     */
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("firebaseEnabled", firebaseEnabled);
        return "register";
    }

    /**
     * パスワードリセット画面
     */
    @GetMapping("/reset-password")
    public String resetPasswordPage(Model model) {
        model.addAttribute("firebaseEnabled", firebaseEnabled);
        return "reset-password";
    }

    /**
     * IDトークン検証・セッション作成
     */
    @PostMapping("/api/auth/verify")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> verifyToken(
            @RequestBody Map<String, String> request,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            String idToken = request.get("idToken");
            log.debug("Received verify request, idToken present: {}", idToken != null && !idToken.isEmpty());

            if (idToken == null || idToken.isEmpty()) {
                response.put("success", false);
                response.put("error", "ID token is required");
                return ResponseEntity.badRequest().body(response);
            }

            // Firebase トークン検証
            log.debug("Calling authService.verifyIdToken...");
            FirebaseToken firebaseToken = authService.verifyIdToken(idToken);
            log.debug("verifyIdToken returned: {}", firebaseToken != null ? "token" : "null");

            if (firebaseToken == null) {
                log.warn("Token verification returned null - check AuthService logs");
                response.put("success", false);
                response.put("error", "Token verification failed");
                return ResponseEntity.status(401).body(response);
            }

            // ユーザー取得または作成
            User user = authService.getOrCreateUser(firebaseToken);

            // 最終ログイン日時を更新
            userService.updateLastLogin(user.getUserId());

            // ログインセッションをセットアップ
            LoginUser loginUser = authService.createLoginUser(user);
            setupLoginSession(loginUser, session);

            response.put("success", true);
            response.put("userId", user.getUserId());
            response.put("email", user.getEmail());

            log.info("User logged in: {}", user.getEmail());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Authentication failed", e);
            response.put("success", false);
            response.put("error", "認証に失敗しました");
            return ResponseEntity.status(401).body(response);
        }
    }

    /**
     * 開発モード用ログイン
     */
    @PostMapping("/api/auth/dev-login")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> devLogin(
            @RequestBody Map<String, String> request,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        if (firebaseEnabled) {
            response.put("success", false);
            response.put("error", "Dev login is not available");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            String email = request.get("email");
            if (email == null || email.isEmpty()) {
                email = "dev@example.com";
            }

            LoginUser loginUser = authService.devModeLogin(email);
            setupLoginSession(loginUser, session);

            response.put("success", true);
            response.put("userId", loginUser.getUserId());
            response.put("email", loginUser.getEmail());

            log.info("Dev user logged in: {}", loginUser.getEmail());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Dev login failed", e);
            response.put("success", false);
            response.put("error", "ログインに失敗しました");
            return ResponseEntity.status(401).body(response);
        }
    }

    /**
     * 現在のユーザー情報取得
     */
    @GetMapping("/api/auth/me")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCurrentUser(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        LoginUser loginUser = (LoginUser) session.getAttribute(SESSION_LOGIN_USER);
        if (loginUser == null) {
            response.put("authenticated", false);
            return ResponseEntity.ok(response);
        }

        response.put("authenticated", true);
        response.put("userId", loginUser.getUserId());
        response.put("email", loginUser.getEmail());
        response.put("name", loginUser.getName());

        return ResponseEntity.ok(response);
    }

    /**
     * ログアウト
     */
    @PostMapping("/api/auth/logout")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> logout(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            // セッションからログイン情報を削除
            session.removeAttribute(SESSION_LOGIN_USER);
            // セッションを無効化
            session.invalidate();
            // Spring Securityのコンテキストをクリア
            SecurityContextHolder.clearContext();

            response.put("success", true);
            log.info("User logged out");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Logout failed", e);
            response.put("success", false);
            response.put("error", "ログアウトに失敗しました");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * ログインセッションをセットアップ（共通処理）
     * セッションへのユーザー情報保存とSpring Security認証設定を行う
     *
     * @param loginUser ログインユーザー情報
     * @param session HTTPセッション
     */
    private void setupLoginSession(LoginUser loginUser, HttpSession session) {
        // セッションにログイン情報を保存
        session.setAttribute(SESSION_LOGIN_USER, loginUser);

        // Spring Securityの認証情報を設定
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        loginUser.getEmail(),
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
