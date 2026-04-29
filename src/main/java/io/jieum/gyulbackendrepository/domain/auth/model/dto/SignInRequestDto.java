package io.jieum.gyulbackendrepository.domain.auth.model.dto;

import jakarta.validation.constraints.NotBlank;

public record SignInRequestDto (
        @NotBlank(message = "이메일은 필수 항목입니다.")
        String email,

        @NotBlank(message = "비밀번호는 필수 항목입니다.")
        String password
) {}
