package com.coursebuddy.controller;

import com.coursebuddy.common.response.ApiResponse;
import com.coursebuddy.domain.dto.AuthLoginRequestDTO;
import com.coursebuddy.domain.dto.AuthRegisterRequestDTO;
import com.coursebuddy.domain.vo.AuthResponseVO;
import com.coursebuddy.service.impl.AuthUserDetailsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "Login and registration endpoints")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class LegacyAuthController {

    private final AuthUserDetailsService authUserDetailsService;
    private final AuthenticationManager authenticationManager;

    @Operation(summary = "注册新用户")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuthResponseVO> register(@Valid @RequestBody AuthRegisterRequestDTO request) {
        AuthResponseVO response = authUserDetailsService.register(request);
        return ApiResponse.success("注册成功", response);
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public ApiResponse<AuthResponseVO> login(@Valid @RequestBody AuthLoginRequestDTO request) {
        AuthResponseVO response = authUserDetailsService.login(request, authenticationManager);
        return ApiResponse.success("登录成功", response);
    }
}
