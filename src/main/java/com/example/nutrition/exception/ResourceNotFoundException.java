package com.example.nutrition.exception;

/**
 * リソースが見つからない場合にスローされる例外
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceType, Long id) {
        super(String.format("%sが見つかりません (ID: %d)", resourceType, id));
    }

    public ResourceNotFoundException(String resourceType, String id) {
        super(String.format("%sが見つかりません (ID: %s)", resourceType, id));
    }
}
