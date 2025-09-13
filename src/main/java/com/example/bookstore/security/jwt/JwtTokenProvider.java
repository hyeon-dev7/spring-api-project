package com.example.bookstore.security.jwt;

import com.example.bookstore.entity.User;
import com.example.bookstore.repository.UserRepository;
import com.example.bookstore.security.LoginUserDetails;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtTokenProvider {

    private long tokenValidityInMilliseconds = 1000 * 60 * 60 * 3; // 3시간 (밀리초 단위)
    private final UserRepository userRepository;
    private final Key key;

    // 생성자. secretKey를 받아 Key 객체 생성 (토큰 서명/검증용)
    public JwtTokenProvider(UserRepository userRepository, @Value("${jwt.secretKey}") String secretKey) {
        this.userRepository = userRepository;
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // 토큰 생성
    public String createToken(String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + tokenValidityInMilliseconds);

        return Jwts.builder()  // JWT 토큰 생성 (subject, iat, exp, 서명 포함)
                .setSubject(username) // 토큰의 주체 (사용자 식별자)
                .setIssuedAt(now) // iat : 현재 시간 (토큰이 발급된 시간)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256) // 서명: 비밀값과 함께 해시값을 HS256 방식으로 암호화
                .compact(); // 토큰 문자열 생성
    }


    // 토큰에서 Claims(JWT 내용, payload) 추출 후 username(subject) 추출
    public String getUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)  // 서명 검증을 위한 키 설정
                .build()
                .parseClaimsJws(token) // 토큰 파싱 및 검증
                .getBody()      // 검증된 토큰의 페이로드(claims) 반환
                .getSubject();  // 클레임에서 subject(username) 추출
    }


    // 토큰 유효성 검사
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token); // 비밀값으로 복호화
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // 토큰 기반으로 Authentication 객체 생성 (스프링 시큐리티 통합용)
    public Authentication getAuthentication(String token) {
        // 토큰에서 username 추출 (subject claim 기반)
        String username = getUsername(token);

        // DB에서 사용자 정보 조회 (없으면 예외 발생)
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자 없음"));

        // 사용자 정보를 기반으로 커스텀 UserDetails 생성
        LoginUserDetails userDetails = new LoginUserDetails(user);

        // 인증 객체 생성 (스프링 시큐리티가 사용하는 Authentication 객체)
        return new UsernamePasswordAuthenticationToken(
                userDetails,                  // principal (인증 주체)
                token,                        // credentials (보통 비밀번호지만 여기선 토큰)
                userDetails.getAuthorities() // 권한 정보 (DB 기반으로 동적 생성됨)
        );
    }

}
