package com.example.foodaiflatformserver.common.handler;

import com.example.foodaiflatformserver.common.exception.NotFoundException;
import com.example.foodaiflatformserver.common.response.ApiErrorResponse;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @DisplayName("검증 실패는 공통 오류 응답 형식으로 반환한다")
    @Test
    void returnsValidationErrorResponse() throws NoSuchMethodException {
        TestRequest request = new TestRequest("not-an-email", "");
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(request, "request");
        bindingResult.addError(new FieldError("request", "email", "이메일 형식이 올바르지 않습니다."));
        bindingResult.addError(new FieldError("request", "name", "이름은 필수입니다."));

        Method method = TestController.class.getDeclaredMethod("validate", TestRequest.class);
        MethodParameter methodParameter = new MethodParameter(method, 0);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<ApiErrorResponse> response = handler.handleMethodArgumentNotValid(exception);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().code()).isEqualTo("INVALID_REQUEST");
        assertThat(response.getBody().message()).isEqualTo("요청 값이 올바르지 않습니다.");
        assertThat(response.getBody().details()).hasSize(2);
        assertThat(response.getBody().details().get(0).field()).isEqualTo("email");
        assertThat(response.getBody().details().get(0).reason()).isEqualTo("이메일 형식이 올바르지 않습니다.");
        assertThat(response.getBody().details().get(1).field()).isEqualTo("name");
        assertThat(response.getBody().details().get(1).reason()).isEqualTo("이름은 필수입니다.");
    }

    @DisplayName("도메인 예외는 공통 오류 응답 형식으로 반환한다")
    @Test
    void returnsDomainExceptionResponse() {
        ResponseEntity<ApiErrorResponse> response = handler.handleApiException(new NotFoundException("식재료를 찾을 수 없습니다."));

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().code()).isEqualTo("NOT_FOUND");
        assertThat(response.getBody().message()).isEqualTo("식재료를 찾을 수 없습니다.");
        assertThat(response.getBody().details()).isEmpty();
    }

    static class TestController {
        void validate(TestRequest request) {
        }
    }

    record TestRequest(
            @Email(message = "이메일 형식이 올바르지 않습니다.") String email,
            @NotBlank(message = "이름은 필수입니다.") String name
    ) {
    }
}
