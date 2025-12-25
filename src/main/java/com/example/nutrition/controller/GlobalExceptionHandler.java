package com.example.nutrition.controller;

import com.example.nutrition.exception.AccessDeniedException;
import com.example.nutrition.exception.ResourceNotFoundException;
import com.example.nutrition.exception.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * グローバル例外ハンドラー
 * アプリケーション全体の例外を統一的に処理
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * アクセス拒否例外を処理
     */
    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied(AccessDeniedException e, RedirectAttributes attrs) {
        log.warn("Access denied: {}", e.getMessage());
        attrs.addFlashAttribute("error", e.getMessage());
        return "redirect:/";
    }

    /**
     * リソース未発見例外を処理
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleResourceNotFound(ResourceNotFoundException e, RedirectAttributes attrs) {
        log.warn("Resource not found: {}", e.getMessage());
        attrs.addFlashAttribute("error", e.getMessage());
        return "redirect:/";
    }

    /**
     * バリデーション例外を処理
     */
    @ExceptionHandler(ValidationException.class)
    public String handleValidation(ValidationException e, RedirectAttributes attrs, HttpServletRequest request) {
        log.warn("Validation error: {}", e.getMessage());
        attrs.addFlashAttribute("error", e.getMessage());
        return redirectToReferer(request);
    }

    /**
     * SecurityException（後方互換性のため残す）を処理
     */
    @ExceptionHandler(SecurityException.class)
    public String handleSecurityException(SecurityException e, RedirectAttributes attrs) {
        log.warn("Security exception: {}", e.getMessage());
        attrs.addFlashAttribute("error", "アクセス権がありません");
        return "redirect:/";
    }

    /**
     * IllegalArgumentException（不正な引数）を処理
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(IllegalArgumentException e, RedirectAttributes attrs, HttpServletRequest request) {
        log.warn("Illegal argument: {}", e.getMessage());
        attrs.addFlashAttribute("error", e.getMessage());
        return redirectToReferer(request);
    }

    /**
     * IllegalStateException（不正な状態）を処理
     */
    @ExceptionHandler(IllegalStateException.class)
    public String handleIllegalStateException(IllegalStateException e, RedirectAttributes attrs) {
        log.warn("Illegal state: {}", e.getMessage());
        attrs.addFlashAttribute("error", "操作を完了できませんでした");
        return "redirect:/login";
    }

    /**
     * バリデーションエラーを処理（Bean Validation）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleMethodArgumentNotValid(MethodArgumentNotValidException e,
            RedirectAttributes attrs, HttpServletRequest request) {
        StringBuilder message = new StringBuilder("入力エラー: ");
        e.getBindingResult().getFieldErrors().forEach(error -> {
            message.append(error.getDefaultMessage()).append(" ");
        });
        log.warn("Validation error: {}", message);
        attrs.addFlashAttribute("error", message.toString().trim());
        return redirectToReferer(request);
    }

    /**
     * 制約違反エラーを処理
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public String handleConstraintViolation(ConstraintViolationException e,
            RedirectAttributes attrs, HttpServletRequest request) {
        StringBuilder message = new StringBuilder("入力エラー: ");
        e.getConstraintViolations().forEach(violation -> {
            message.append(violation.getMessage()).append(" ");
        });
        log.warn("Constraint violation: {}", message);
        attrs.addFlashAttribute("error", message.toString().trim());
        return redirectToReferer(request);
    }

    /**
     * その他のRuntimeExceptionを処理
     */
    @ExceptionHandler(RuntimeException.class)
    public String handleRuntimeException(RuntimeException e, RedirectAttributes attrs) {
        log.error("Unexpected error", e);
        attrs.addFlashAttribute("error", "予期しないエラーが発生しました");
        return "redirect:/";
    }

    /**
     * リファラーにリダイレクト（なければトップページ）
     */
    private String redirectToReferer(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isEmpty()) {
            // 同一ホストのみ許可（オープンリダイレクト対策）
            String serverName = request.getServerName();
            if (referer.contains(serverName)) {
                return "redirect:" + referer;
            }
        }
        return "redirect:/";
    }
}
