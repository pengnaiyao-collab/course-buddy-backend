package com.coursebuddy.controller;

import com.coursebuddy.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 自定义错误控制器
 * 接管 Spring Boot 默认的错误处理，防止 Tomcat 在响应已提交时尝试处理错误页面
 */
@Slf4j
@RestController
public class CustomErrorController implements ErrorController {

    @RequestMapping(value = "/error", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<Void>> handleError(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        String message = (String) request.getAttribute("javax.servlet.error.message");
        Throwable throwable = (Throwable) request.getAttribute("javax.servlet.error.exception");

        if (statusCode == null) {
            statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        }

        if (message == null) {
            message = "Internal Server Error";
        }

        log.error("Error occurred: status={}, message={}, exception={}", 
                statusCode, message, throwable != null ? throwable.getMessage() : "none");

        // 返回标准的 API 错误响应
        return ResponseEntity.status(statusCode)
            .contentType(MediaType.APPLICATION_JSON)
            .body(ApiResponse.error(statusCode, message));
    }
}
