package com.example.foodaiplatformserver.common.handler;

import com.example.foodaiplatformserver.common.ApiErrorCode;
import com.example.foodaiplatformserver.common.exception.ApiException;
import com.example.foodaiplatformserver.common.response.ApiErrorDetail;
import com.example.foodaiplatformserver.common.response.ApiErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Comparator;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(ApiException exception) {
        ApiErrorCode errorCode = exception.getErrorCode();

        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ApiErrorResponse.of(errorCode, exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        List<ApiErrorDetail> details = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .sorted(Comparator.comparing(FieldError::getField))
                .map(fieldError -> new ApiErrorDetail(toSnakeCaseField(fieldError.getField()), fieldError.getDefaultMessage()))
                .toList();

        return badRequest(ApiErrorCode.INVALID_REQUEST.getMessage(), details);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException exception) {
        List<ApiErrorDetail> details = exception.getConstraintViolations()
                .stream()
                .map(violation -> new ApiErrorDetail(toSnakeCaseField(violation.getPropertyPath().toString()), violation.getMessage()))
                .toList();

        return badRequest(ApiErrorCode.INVALID_REQUEST.getMessage(), details);
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiErrorResponse> handleRequestParsingException(Exception exception) {
        return badRequest(ApiErrorCode.INVALID_REQUEST.getMessage(), List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(Exception exception) {
        ApiErrorCode errorCode = ApiErrorCode.INTERNAL_SERVER_ERROR;

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.of(errorCode, errorCode.getMessage()));
    }

    private ResponseEntity<ApiErrorResponse> badRequest(String message, List<ApiErrorDetail> details) {
        ApiErrorCode errorCode = ApiErrorCode.INVALID_REQUEST;

        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ApiErrorResponse.of(errorCode, message, details));
    }

    private String toSnakeCaseField(String rawField) {
        String field = rawField;
        int lastDotIndex = field.lastIndexOf('.');
        if (lastDotIndex >= 0) {
            field = field.substring(lastDotIndex + 1);
        }

        StringBuilder snakeCase = new StringBuilder();
        for (int index = 0; index < field.length(); index++) {
            char current = field.charAt(index);
            if (Character.isUpperCase(current)) {
                if (!snakeCase.isEmpty()) {
                    snakeCase.append('_');
                }
                snakeCase.append(Character.toLowerCase(current));
                continue;
            }
            snakeCase.append(current);
        }
        return snakeCase.toString();
    }
}
