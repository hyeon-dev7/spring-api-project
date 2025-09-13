package com.example.bookstore.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
// 요청에서 토큰 추출, 인증 처리
    private final JwtTokenProvider jwtTokenProvider;
    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider) {
        this.jwtTokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Authorization 헤더에서 토큰 추출
        String token = extractToken(request);

        // 2. 토큰이 있고 유효하면
        if (token != null && jwtTokenProvider.validateToken(token)) {
            // 3. 토큰에서 Authentication 객체 생성
            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            // 4. 시큐리티 컨텍스트에 인증 정보 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        // 5. 다음 필터로 진행
        filterChain.doFilter(request, response);
    }


    // 요청에서 토큰 추출
    public String extractToken(HttpServletRequest request) {
        // 쿠키에서 먼저 시도
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        // Authorization 헤더에서 시도
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 이후의 토큰 값만 추출
        }
        return null;
    }
}