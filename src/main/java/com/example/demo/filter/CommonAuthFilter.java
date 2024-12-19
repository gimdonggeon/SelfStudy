package com.example.demo.filter;

import com.example.demo.exception.UnauthorizedException;
import jakarta.servlet.Filter;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;

import java.util.Optional;

public interface CommonAuthFilter extends Filter {

    default HttpSession findHttpSession(ServletRequest request) {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpSession session = httpServletRequest.getSession(false);

        String requestURI = httpServletRequest.getRequestURI();
        if (requestURI.equals("/users") || requestURI.equals("/users/login")) {
            return null;
        }

        if (session == null) {
            throw new UnauthorizedException(HttpStatus.UNAUTHORIZED, "로그인 필요");
        }
        return session;
    }
}