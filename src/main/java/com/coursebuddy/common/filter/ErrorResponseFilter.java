package com.coursebuddy.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 错误响应处理过滤器
 * 防止 Tomcat 在响应已提交时尝试处理错误页面，导致的异常堆栈
 */
@Slf4j
@Component
public class ErrorResponseFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (IOException e) {
            // 如果响应已提交，则直接忽略后续异常
            if (response.isCommitted()) {
                log.debug("响应已提交，忽略 IO 异常: {}", e.getMessage());
                return;
            }
            throw e;
        } catch (ServletException e) {
            // 如果响应已提交，则直接忽略后续异常
            if (response.isCommitted()) {
                log.debug("响应已提交，忽略 Servlet 异常: {}", e.getMessage());
                return;
            }
            throw e;
        }
    }
}
