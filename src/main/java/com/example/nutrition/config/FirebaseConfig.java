package com.example.nutrition.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Firebase Admin SDK設定
 * Firebase認証の初期化とBeanの提供を担当
 */
@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.config.path:firebase-service-account.json}")
    private String firebaseConfigPath;

    @Getter
    @Value("${firebase.enabled:true}")
    private boolean firebaseEnabled;

    private boolean initialized = false;

    /**
     * アプリケーション起動時にFirebaseを初期化
     */
    @PostConstruct
    public void initialize() {
        if (!firebaseEnabled) {
            log.warn("Firebase is disabled. Running in development mode.");
            return;
        }

        log.info("Firebase credentials path: {}", firebaseConfigPath);

        File file = new File(firebaseConfigPath);
        if (!file.exists()) {
            log.error("Firebase credentials file not found: {}", file.getAbsolutePath());
            return;
        }

        try (InputStream serviceAccount = new FileInputStream(file)) {
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);
                initialized = true;
                log.info("Firebase initialized successfully");
            } else {
                initialized = true;
                log.info("Firebase already initialized");
            }
        } catch (IOException e) {
            log.error("Failed to initialize Firebase: {}", e.getMessage());
        }
    }

    /**
     * FirebaseAuthインスタンスを取得
     * @return FirebaseAuth、初期化失敗時はnull
     */
    public FirebaseAuth getFirebaseAuth() {
        if (!firebaseEnabled || !initialized) {
            return null;
        }
        return FirebaseAuth.getInstance();
    }
}
