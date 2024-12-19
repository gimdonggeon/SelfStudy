package com.example.demo.interceptor;

import com.example.demo.constants.GlobalConstants;
import com.example.demo.dto.Authentication;
import com.example.demo.entity.Role;
import com.example.demo.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminRoleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws UnauthorizedException {
        String requestURI = request.getRequestURI();

        if (requestURI.startsWith("/admins")) {
            HttpSession session = request.getSession(false);
            if (session == null) {
                throw new UnauthorizedException(HttpStatus.UNAUTHORIZED, "세션이 끊어졌습니다.");
            }

            Authentication authentication = (Authentication) session.getAttribute(GlobalConstants.USER_AUTH);
            if (authentication == null) {
                throw new UnauthorizedException(HttpStatus.UNAUTHORIZED, "로그인된 사용자만 접근 가능합니다.");
            }

            Role role = authentication.getRole();

            if (role != Role.ADMIN) {
                throw new UnauthorizedException(HttpStatus.UNAUTHORIZED, "ADMIN권한이 필요합니다.");
            }
        }
        return true;
    }
}
