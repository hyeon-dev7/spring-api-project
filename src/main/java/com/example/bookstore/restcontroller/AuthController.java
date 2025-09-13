package com.example.bookstore.restcontroller;

import com.example.bookstore.dto.LoginRequest;
import com.example.bookstore.dto.UserDTO;
import com.example.bookstore.entity.User;
import com.example.bookstore.repository.UserRepository;
import com.example.bookstore.security.jwt.JwtTokenProvider;
import com.example.bookstore.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider, UserRepository userRepository, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @PostMapping("/api/auth/register")
    public ResponseEntity<?> registerUser(@RequestBody @Valid UserDTO userDTO,
                                          BindingResult bindingResult) {

        Optional<User> optionalUser = userRepository.findByUsername(userDTO.getUsername());
        if (optionalUser.isPresent()) {
            bindingResult.addError(new FieldError("UserDTO", "username", "이미 존재하는 아이디입니다."));
        }
        Optional<User> optionalUser2 = userRepository.findByNickname(userDTO.getNickname());
        if (optionalUser2.isPresent()) {
            bindingResult.addError(new FieldError("UserDTO", "username", "이미 존재하는 별명입니다."));
        }
        if (bindingResult.hasErrors()) {
            List<Map<String, String>> errors = bindingResult.getFieldErrors().stream()
                    .map(error -> Map.of( // Map.of : 불변 Map 만드는 메서드
                            "field", error.getField(),
                            "message", error.getDefaultMessage()
                    ))
                    .toList(); // Map != JSON, 하지만 spring에서 자동 변환해준다.
            return ResponseEntity.badRequest().body(errors);
        }
        try {
            userService.register(userDTO);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "회원가입 중 오류가 발생했습니다."));
        }
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request,
                                   @RequestParam(value = "redirect", required = false) String redirect,
                                   HttpServletResponse response) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            String jwt = jwtTokenProvider.createToken(auth.getName());

            // 쿠키 설정
            ResponseCookie cookie = ResponseCookie.from("token", jwt) // "쿠키이름", 값
                    .httpOnly(true)
                    .secure(false)
                    .sameSite("Lax")
                    .path("/")  // 모든 경로에 쿠키 전송
                    .maxAge(3600 * 3) // 3시간
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            // 리다이렉트 url
            String url = (redirect != null) ? redirect : "/";

            // JSON 응답에 토큰 포함
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "accessToken", jwt,
                    "redirect", url
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "아이디 또는 비밀번호를 확인해주세요."));
        }
    }

    @PostMapping("/api/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        try {
            ResponseCookie cookie = ResponseCookie.from("token", "")
                    .httpOnly(true)
                    .secure(false)
                    .sameSite("Lax")
                    .path("/")
                    .maxAge(0)
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "로그아웃 처리 중 오류가 발생했습니다. 다시 시도해주세요."));
        }
    }
}
