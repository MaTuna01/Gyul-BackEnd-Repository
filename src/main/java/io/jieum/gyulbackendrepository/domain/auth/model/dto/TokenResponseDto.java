package io.jieum.gyulbackendrepository.domain.auth.model.dto;

public record TokenResponseDto(
        String accessToken,
        String refreshToken
) {
}
