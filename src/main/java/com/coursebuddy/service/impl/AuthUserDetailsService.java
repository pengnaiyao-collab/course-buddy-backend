package com.coursebuddy.service.impl;

import com.coursebuddy.common.exception.BusinessException;
import com.coursebuddy.common.security.JwtUtil;
import com.coursebuddy.domain.auth.Role;
import com.coursebuddy.domain.auth.User;
import com.coursebuddy.domain.dto.AuthLoginRequestDTO;
import com.coursebuddy.domain.dto.AuthRegisterRequestDTO;
import com.coursebuddy.domain.vo.AuthResponseVO;
import com.coursebuddy.mapper.AuthUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthUserDetailsService implements UserDetailsService {

    private final AuthUserMapper userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("找不到该用户: " + username));
    }

    @Transactional
    public AuthResponseVO register(AuthRegisterRequestDTO request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(409, "用户名已存在");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(409, "该邮箱已被注册");
        }

        Role role = request.getRole() != null ? request.getRole() : Role.STUDENT;

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(role)
                .build();

        userRepository.insert(user);
        String token = jwtUtil.generateToken(user);
        return AuthResponseVO.of(token, user);
    }

    public AuthResponseVO login(AuthLoginRequestDTO request, AuthenticationManager authenticationManager) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("找不到该用户"));
        String token = jwtUtil.generateToken(user);
        return AuthResponseVO.of(token, user);
    }
}
