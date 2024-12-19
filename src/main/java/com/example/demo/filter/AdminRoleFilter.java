package com.example.demo.filter;

import com.example.demo.constants.GlobalConstants;
import com.example.demo.dto.Authentication;
import com.example.demo.entity.Role;
import com.example.demo.exception.UnauthorizedException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;

@RequiredArgsConstructor
@Component
public class AdminRoleFilter implements CommonAuthFilter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
        throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String requestURI = httpServletRequest.getRequestURI();

        if (requestURI.equals("/users") || requestURI.equals("/users/login") || requestURI.equals("/users/logout")) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        if (requestURI.startsWith("/admins")) {
            HttpSession session = findHttpSession(servletRequest);
            Authentication authentication = (Authentication) session.getAttribute(GlobalConstants.USER_AUTH);

            if (authentication == null) {
                throw new UnauthorizedException(HttpStatus.UNAUTHORIZED, "로그인된 사용자만 접근할 수 있습니다.");
            }

            Role clientRole = authentication.getRole();

            if (clientRole != Role.ADMIN) {
                throw new UnauthorizedException(HttpStatus.UNAUTHORIZED, "ADMIN권한이 필요합니다.");
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
