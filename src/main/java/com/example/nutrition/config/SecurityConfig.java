package com.example.nutrition.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

/**
 * Spring Security設定
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${spring.h2.console.enabled:false}")
    private boolean h2ConsoleEnabled;

    @Value("${firebase.enabled:true}")
    private boolean firebaseEnabled;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // CSRF設定
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName("_csrf");

        http
            // URL別アクセス制御
            .authorizeHttpRequests(auth -> {
                auth
                    // 認証不要URL
                    .requestMatchers("/login", "/register", "/reset-password").permitAll()
                    .requestMatchers("/api/auth/verify").permitAll()
                    .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                    .requestMatchers("/error").permitAll();

                // 開発モード時のみdev-loginを許可
                if (!firebaseEnabled) {
                    auth.requestMatchers("/api/auth/dev-login").permitAll();
                }

                // H2コンソール（開発時のみ）
                if (h2ConsoleEnabled) {
                    auth.requestMatchers("/h2-console/**").permitAll();
                }

                // その他は認証必須
                auth.anyRequest().authenticated();
            })
            // ログイン設定
            .formLogin(form -> form
                .loginPage("/login")
                .permitAll()
            )
            // ログアウト設定
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "NUTRITION_SESSION")
                .permitAll()
            )
            // CSRF設定
            .csrf(csrf -> {
                csrf
                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .csrfTokenRequestHandler(requestHandler)
                    // 認証APIはCSRF除外
                    .ignoringRequestMatchers("/api/auth/verify", "/api/auth/logout");

                // 開発モード時のみdev-loginをCSRF除外
                if (!firebaseEnabled) {
                    csrf.ignoringRequestMatchers("/api/auth/dev-login");
                }

                // H2コンソール（開発時のみ）
                if (h2ConsoleEnabled) {
                    csrf.ignoringRequestMatchers("/h2-console/**");
                }
            })
            // セキュリティヘッダー設定
            .headers(headers -> {
                // H2コンソール用のフレーム許可（開発時のみ）
                if (h2ConsoleEnabled) {
                    headers.frameOptions(frame -> frame.sameOrigin());
                } else {
                    headers.frameOptions(frame -> frame.deny());
                }

                // XSS保護
                headers.xssProtection(xss -> xss
                    .headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)
                );

                // Content-Type sniffing防止
                headers.contentTypeOptions(content -> {});
            })
            // セッション管理
            .sessionManagement(session -> session
                .maximumSessions(5)
                .expiredUrl("/login?expired")
            )
            // セキュリティコンテキストをセッションに保存
            .securityContext(context -> context
                .requireExplicitSave(false)
            );

        return http.build();
    }
}
