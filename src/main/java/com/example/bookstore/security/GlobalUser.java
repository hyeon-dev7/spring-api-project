package com.example.bookstore.security;

import com.example.bookstore.dto.UserDTO;
import com.example.bookstore.entity.User;
import com.example.bookstore.service.MapperService;
import com.example.bookstore.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Optional;

@ControllerAdvice
public class GlobalUser {
    private final UserService userService;
    private final MapperService mapperService;

    public GlobalUser(UserService userService, MapperService mapperService) {
        this.userService = userService;
        this.mapperService = mapperService;
    }

    @ModelAttribute("userDTO") // 반환값을 모델에 자동으로 추가함
    public UserDTO addUserToModel(HttpServletRequest req) {
        Optional<User> optionalUser = userService.getCurrentUser(req);
        if(optionalUser.isPresent()){
            return mapperService.userToDTO(optionalUser.get());
        }
        return null; // 비로그인 상태
    }
}