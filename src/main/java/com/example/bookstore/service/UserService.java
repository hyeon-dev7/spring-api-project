package com.example.bookstore.service;

import com.example.bookstore.dto.UserDTO;
import com.example.bookstore.entity.User;
import com.example.bookstore.repository.UserRepository;
import com.example.bookstore.security.jwt.JwtAuthenticationFilter;
import com.example.bookstore.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Transactional
    public User register(UserDTO userDTO){
        User user = User.builder()
                .username(userDTO.getUsername())
                .password(passwordEncoder.encode(userDTO.getPassword()))
                .name(userDTO.getName())
                .nickname(userDTO.getNickname())
                .build();
        return userRepository.save(user);
    }

    public Optional<User> getCurrentUser(HttpServletRequest request){
        String token = jwtAuthenticationFilter.extractToken(request);
        if (token!=null && jwtTokenProvider.validateToken(token)){
            String username = jwtTokenProvider.getUsername(token);
            return userRepository.findByUsername(username);
        }
        return Optional.empty();
    }

}
