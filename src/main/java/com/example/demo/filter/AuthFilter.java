package com.example.demo.filter;

import com.example.demo.constants.GlobalConstants;
import com.example.demo.exception.UnauthorizedException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuthFilter implements CommonAuthFilter {

    private static final String[] EXCLUDED_PATHS = {"/users", "/users/login", "users/logout"};  // 여기에 /users 및 /users/login 추가

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String requestURI = httpServletRequest.getRequestURI();

        // 경로가 제외된 경로인지 확인
        if (isExcludedPath(requestURI)) {
            filterChain.doFilter(servletRequest, servletResponse);  // 필터를 건너뛰기
            return;
        }

        if (!requestURI.equals("/users/logout")) {
            HttpSession session = httpServletRequest.getSession(false);
            if (session == null || session.getAttribute(GlobalConstants.USER_AUTH) == null) {
                throw new UnauthorizedException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
            }
        }else {
            findHttpSession(servletRequest);
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    private boolean isExcludedPath(String requestURI) {
        for (String excludedPath : EXCLUDED_PATHS) {
            if (requestURI.startsWith(excludedPath)) {
                return true;  // 제외된 경로
            }
        }
        return false;
    }
}
