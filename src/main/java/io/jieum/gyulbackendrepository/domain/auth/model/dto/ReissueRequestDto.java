package io.jieum.gyulbackendrepository.domain.auth.model.dto;

import jakarta.validation.constraints.NotBlank;

public record ReissueRequestDto(
        @NotBlank(message = "Refresh Token은 필수 항목입니다.")
        String refreshToken
) {
}
