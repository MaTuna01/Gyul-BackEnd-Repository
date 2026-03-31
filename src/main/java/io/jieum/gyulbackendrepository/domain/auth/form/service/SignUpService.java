package io.jieum.gyulbackendrepository.domain.auth.form.service;

import io.jieum.gyulbackendrepository.domain.auth.model.dto.SignUpRequestDto;
import io.jieum.gyulbackendrepository.domain.user.model.entity.Member;
import io.jieum.gyulbackendrepository.domain.user.repository.MemberRepository;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class SignUpService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public Member CreateUser(SignUpRequestDto signUpRequest) {

        // 입력 이메일 기준 회원 중복검사(이름은 동명이인을 감안하여 중복검사 로직 구현X)
        if(memberRepository.existsByEmail(signUpRequest.email())){
            throw new ValidationException("Email already exists");
        }

        Member signUpMember = Member.builder()
                .email(signUpRequest.email())
                .password(passwordEncoder.encode(signUpRequest.password()))
                .name(signUpRequest.name())
                .createdAt(LocalDateTime.now())
                .gender(signUpRequest.gender())
                .build();

        memberRepository.save(signUpMember);
        return signUpMember;

    }
}
