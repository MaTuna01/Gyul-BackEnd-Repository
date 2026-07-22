package io.jieum.gyulbackendrepository.domain.auth.form.controller;

import io.jieum.gyulbackendrepository.domain.auth.form.service.SignUpService;
import io.jieum.gyulbackendrepository.domain.auth.model.dto.SignUpRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class SignUpController {

    private final SignUpService signUpService;

    @PostMapping("/signup")
    public ResponseEntity<String> CreateUser(
            @Valid @RequestBody SignUpRequestDto requestDto){
        signUpService.CreateUser(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입 성공");
    }
}

