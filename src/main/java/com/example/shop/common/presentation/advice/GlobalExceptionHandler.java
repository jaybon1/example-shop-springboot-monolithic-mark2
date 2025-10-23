package com.example.shop.common.presentation.advice;

import com.example.shop.common.infrastructure.constants.Constants;
import com.example.shop.common.presentation.dto.ApiDto;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiDto<Object>> handleMissingServletRequestParameterException(Exception e) {
        return new ResponseEntity<>(
                ApiDto.builder()
                        .code(Constants.ApiCode.MISSING_SERVLET_REQUEST_PARAMETER_EXCEPTION.toString())
                        .message(e.getMessage())
                        .build(),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiDto<Object>> handleBindException(BindException e) {
        Map<String, String> errorMap = new HashMap<>();

        e.getBindingResult().getFieldErrors().forEach(fieldError -> {
            errorMap.put(fieldError.getField(), fieldError.getDefaultMessage());
        });

        return new ResponseEntity<>(
                ApiDto.builder()
                        .code(Constants.ApiCode.BIND_EXCEPTION.toString())
                        .message("요청한 데이터가 유효하지 않습니다.")
                        .data(errorMap)
                        .build(),
                HttpStatus.BAD_REQUEST
        );

    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiDto<Object>> handleBindException(ConstraintViolationException exception) {
        Map<String, String> errorMap = new HashMap<>();
        exception.getConstraintViolations().forEach(constraintViolation -> {
            List<Path.Node> pathNodeList = StreamSupport
                    .stream(constraintViolation.getPropertyPath().spliterator(), false)
                    .toList();
            errorMap.put(pathNodeList.get(pathNodeList.size() - 1).getName(), constraintViolation.getMessage());
        });
        return new ResponseEntity<>(
                ApiDto.builder()
                        .code(Constants.ApiCode.CONSTRAINT_VIOLATION_EXCEPTION.toString())
                        .message("요청한 데이터가 유효하지 않습니다.")
                        .data(errorMap)
                        .build(),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiDto<Object>> handleHttpMessageNotReadableException(Exception e) {
        if (e.getMessage().contains("Required request body is missing")) {
            return new ResponseEntity<>(
                    ApiDto.builder()
                            .code(Constants.ApiCode.HTTP_MESSAGE_NOT_READABLE_EXCEPTION.toString())
                            .message("RequestBody가 없습니다.")
                            .build(),
                    HttpStatus.BAD_REQUEST
            );
        }
        if (e.getMessage().contains("Enum class: ")) {
            return new ResponseEntity<>(
                    ApiDto.builder()
                            .code(Constants.ApiCode.HTTP_MESSAGE_NOT_READABLE_EXCEPTION.toString())
                            .message("Type 매개변수를 확인하세요.")
                            .build(),
                    HttpStatus.BAD_REQUEST
            );
        }
        return new ResponseEntity<>(
                ApiDto.builder()
                        .code(Constants.ApiCode.HTTP_MESSAGE_NOT_READABLE_EXCEPTION.toString())
                        .message("RequestBody를 형식에 맞추어 주세요.")
                        .build(),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiDto<Object>> handleHttpRequestMethodNotSupportedException(Exception e) {
        return new ResponseEntity<>(
                ApiDto.builder()
                        .code(Constants.ApiCode.HTTP_REQUEST_METHOD_NOT_SUPPORT_EXCEPTION.toString())
                        .message("엔드포인트가 요청하신 메소드에 대해 지원하지 않습니다. 메소드, 엔드포인트, PathVariable을 확인하세요.")
                        .build(),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiDto<Object>> handleMethodArgumentTypeMismatchException(Exception e) {
        return new ResponseEntity<>(
                ApiDto.builder()
                        .code(Constants.ApiCode.METHOD_ARGUMENT_TYPE_MISMATCH_EXCEPTION.toString())
                        .message("PathVariable, QueryString의 타입을 확인하세요.")
                        .build(),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(ConversionFailedException.class)
    public ResponseEntity<ApiDto<Object>> handleConversionFailedException(Exception e) {
        if (e.getMessage().contains("persistence.Enumerated")) {
            return new ResponseEntity<>(
                    ApiDto.builder()
                            .code(Constants.ApiCode.CONVERSION_FAILED_EXCEPTION.toString())
                            .message("status를 정확히 입력해주세요.")
                            .build(),
                    HttpStatus.BAD_REQUEST
            );
        }
        return new ResponseEntity<>(
                ApiDto.builder()
                        .code(Constants.ApiCode.CONVERSION_FAILED_EXCEPTION.toString())
                        .message("PathVariable, QueryString, ResponseBody를 확인하세요.")
                        .build(),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiDto<Object>> handleException(Exception e) {
        e.printStackTrace();
        // ---
        // TODO: 로깅
        // ---
        return new ResponseEntity<>(
                ApiDto.builder()
                        .code(Constants.ApiCode.EXCEPTION.toString())
                        .message(e.getMessage())
                        .build(),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

}
