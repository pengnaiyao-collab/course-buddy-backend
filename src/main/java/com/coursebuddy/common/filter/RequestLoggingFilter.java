package com.coursebuddy.common.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 全局请求日志过滤器，记录所有HTTP请求
 */
@Slf4j
@Component
public class RequestLoggingFilter implements Filter {

    @Override
    public void init(FilterConfig config) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String method = httpRequest.getMethod();
        String uri = httpRequest.getRequestURI();
        String queryString = httpRequest.getQueryString();
        
        log.info("📨 收到请求: {} {} {}", method, uri, queryString != null ? "?" + queryString : "");
        
        try {
            chain.doFilter(request, response);
        } catch (Exception e) {
            log.error("❌ 请求处理异常: {} {}", method, uri, e);
            throw e;
        }
    }

    @Override
    public void destroy() {
    }
}
