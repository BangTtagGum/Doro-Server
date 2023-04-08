package com.example.DoroServer.global.auth.dto;

import com.example.DoroServer.domain.user.entity.StudentStatus;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import lombok.Getter;

public class AuthRequestDto {

    @Getter
    public static class JoinDto{
        @NotBlank
        private String account;

        @NotBlank
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9]).{8,}$", message = "영문, 숫자 포함 8자 이상으로 입력해주세요.")
        private String password;

        @NotBlank
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9]).{8,}$", message = "영문, 숫자 포함 8자 이상으로 입력해주세요.")
        private String passwordCheck;

        @NotBlank
        private String name;

        @NotBlank
        private int age;

        @NotBlank
        private String gender;

        @NotBlank
        @Pattern(regexp = "^01([016789])-?([0-9]{3,4})-?([0-9]{4})$", message = "올바른 휴대폰 번호 형식이 아닙니다.")
        private String phone;

        @NotBlank
        private String school;

        @NotBlank
        private String studentId;

        @NotBlank
        private String major;

        @NotBlank
        private StudentStatus studentStatus;

        @NotBlank
        private int generation;

        @NotBlank
        private String role;

        private String profileImg;
    }

    @Getter
    public static class SendAuthNumDto{
        @NotBlank
        private String name;

        @NotBlank
        @Pattern(regexp = "^01([016789])-?([0-9]{3,4})-?([0-9]{4})$", message = "올바른 휴대폰 번호 형식이 아닙니다.")
        private String phone;
    }

}
