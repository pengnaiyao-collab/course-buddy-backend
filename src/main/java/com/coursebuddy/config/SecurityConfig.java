package com.coursebuddy.config;

import com.coursebuddy.common.security.JwtAuthenticationFilter;
import com.coursebuddy.service.impl.AuthUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.beans.factory.annotation.Value;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Spring Security 核心配置类
 * 配置全局安全策略，包括无状态 Session、白名单路径、JWT 过滤器拦截以及认证提供者
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthUserDetailsService authUserDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.cors.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;

    // 放行的公开路径（登录注册、接口文档、健康检查、WebSocket等）
    private static final String[] PUBLIC_PATHS = {
            "/auth/**",
            "/v1/auth/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/actuator/health",
            "/actuator/**",
            "/ws/**",
            "/error"
    };

    /**
     * 配置安全过滤链
     * 1. 禁用 CSRF
     * 2. 启用无状态的会话管理 (STATELESS)
     * 3. 配置 URL 访问控制权限
     * 4. 挂载自定义的 JWT 过滤器在 UsernamePasswordAuthenticationFilter 之前
     *
     * @param http HttpSecurity 构建器
     * @return 构建完成的 SecurityFilterChain
     * @throws Exception 配置过程中的异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> {
                    exception
                            .authenticationEntryPoint((request, response, authException) -> {
                                // 检查响应是否已提交，防止重复处理
                                if (response.isCommitted()) {
                                    return;
                                }
                                response.setContentType("application/json;charset=UTF-8");
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                response.getWriter().write("{\"code\":401,\"message\":\"用户未登录或认证已过期\",\"data\":null}");
                            })
                            .accessDeniedHandler((request, response, accessDeniedException) -> {
                                // 检查响应是否已提交，防止重复处理
                                if (response.isCommitted()) {
                                    return;
                                }
                                response.setContentType("application/json;charset=UTF-8");
                                var authentication = SecurityContextHolder.getContext().getAuthentication();
                                if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
                                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                    response.getWriter().write("{\"code\":401,\"message\":\"用户未登录或认证已过期\",\"data\":null}");
                                } else {
                                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                    response.getWriter().write("{\"code\":403,\"message\":\"无权访问该资源\",\"data\":null}");
                                }
                            });
                })
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        // 放行课程列表相关的 GET 请求
                        .requestMatchers(HttpMethod.GET, "/v1/courses-catalog/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/courses/**").permitAll()
                    // 放行头像预览（仅限 avatar 分类文件）
                    .requestMatchers(HttpMethod.GET, "/v1/files/avatar", "/files/avatar").permitAll()
                        // 放行健康检查端点
                        .requestMatchers(HttpMethod.GET, "/actuator/**").permitAll()
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 配置认证提供者
     * 使用 DaoAuthenticationProvider，并注入自定义的 UserDetailsService 和密码加密器
     *
     * @return 认证提供者实例
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(authUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    /**
     * 暴露 AuthenticationManager Bean 供 AuthController 使用
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * CORS 全局配置
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toList());
        if (origins.contains("*")) {
            configuration.setAllowedOriginPatterns(List.of("*"));
            configuration.setAllowCredentials(false);
        } else {
            configuration.setAllowedOrigins(origins);
            configuration.setAllowCredentials(true);
        }
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
