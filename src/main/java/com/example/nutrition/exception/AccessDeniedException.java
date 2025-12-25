package com.example.nutrition.exception;

/**
 * アクセス権限がない場合にスローされる例外
 */
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }

    public AccessDeniedException(String resourceType, String action) {
        super(String.format("自分の%sのみ%sできます", resourceType, action));
    }
}
