package com.example.demo.interceptor;

import com.example.demo.constants.GlobalConstants;
import com.example.demo.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final String[] EXCLUDED_PATHS = {"/users", "/users/login"};  // 여기에 /users 및 /users/login 추가

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws UnauthorizedException {
        String requestURI = request.getRequestURI();

        // 경로가 제외된 경로인지 확인
        if (isExcludedPath(requestURI)) {
            return true;  // 인터셉터를 건너뛰기
        }

        if (!requestURI.equals("/users/logout")) {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute(GlobalConstants.USER_AUTH) == null) {
                throw new UnauthorizedException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
            }
        }
        return true;
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