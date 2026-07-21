package io.jieum.gyulbackendrepository.domain.auth.model.dto;

import io.jieum.gyulbackendrepository.domain.user.model.entity.Gender;
import jakarta.validation.constraints.NotNull;

// 소셜 로그인 최초 가입 시 추가정보(성별) 입력 요청
public record ProfileUpdateRequestDto(

        @NotNull(message = "성별은 필수 항목입니다.")
        Gender gender
) {
}
