package com.example.nutrition.exception;

/**
 * バリデーションエラーの場合にスローされる例外
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}
