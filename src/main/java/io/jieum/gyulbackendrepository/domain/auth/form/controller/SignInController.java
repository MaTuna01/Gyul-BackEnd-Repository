package io.jieum.gyulbackendrepository.domain.auth.form.controller;

import io.jieum.gyulbackendrepository.domain.auth.form.service.SignInService;
import io.jieum.gyulbackendrepository.domain.auth.model.dto.ReissueRequestDto;
import io.jieum.gyulbackendrepository.domain.auth.model.dto.SignInRequestDto;
import io.jieum.gyulbackendrepository.domain.auth.model.dto.TokenResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
//인증이 필요한 경로이므로 security config에서 permitAll에 포함하지 말것
@RequestMapping("/auth")
public class SignInController {
    private final SignInService signInService;

    // 로그인
    @PostMapping("/signin")
    public ResponseEntity<TokenResponseDto> signIn(
            @Valid @RequestBody SignInRequestDto request
    ) {
        TokenResponseDto tokens = signInService.signIn(request);
        return ResponseEntity.ok(tokens);
    }

    //Access Token 재발급
    @PostMapping("/reissue")
    public ResponseEntity<TokenResponseDto> reissue(
            @Valid @RequestBody ReissueRequestDto request
    ) {
        TokenResponseDto tokens = signInService.reissue(request);
        return ResponseEntity.ok(tokens);
    }

    //로그아웃
    @PostMapping("/signout")
    public ResponseEntity<String> signOut(
            @AuthenticationPrincipal String email
    ) {
        signInService.signOut(email);
        return ResponseEntity.ok("로그아웃");
    }
}
