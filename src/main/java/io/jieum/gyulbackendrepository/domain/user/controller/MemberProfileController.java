package io.jieum.gyulbackendrepository.domain.user.controller;

import io.jieum.gyulbackendrepository.domain.auth.model.dto.ProfileUpdateRequestDto;
import io.jieum.gyulbackendrepository.domain.user.service.MemberProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberProfileController {

    private final MemberProfileService memberProfileService;

    // 추가정보(성별) 입력 — JWT 인증 필요, principal은 이메일
    @PutMapping("/me/profile")
    public ResponseEntity<String> updateProfile(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody ProfileUpdateRequestDto request
    ) {
        memberProfileService.updateProfile(email, request);
        return ResponseEntity.ok("추가정보 입력 완료");
    }
}
