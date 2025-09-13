package com.example.bookstore.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    @Email(message = "올바른 이메일을 입력해주세요.")
    @NotBlank(message="아이디(이메일)를 입력해주세요.")
    private String username;
    @NotBlank(message="비밀번호를 입력해주세요.")
    @Size(min=4, message="비밀번호는 4글자 이상이어야 합니다.")
    private String password;
    @NotBlank(message="이름를 입력해주세요.")
    private String name;
    @NotBlank(message="별명을 입력해주세요.")
    private String nickname;
}
