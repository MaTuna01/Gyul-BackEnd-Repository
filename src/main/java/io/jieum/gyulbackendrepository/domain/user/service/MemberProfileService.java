package io.jieum.gyulbackendrepository.domain.user.service;

import io.jieum.gyulbackendrepository.domain.auth.model.dto.ProfileUpdateRequestDto;
import io.jieum.gyulbackendrepository.domain.user.model.entity.Member;
import io.jieum.gyulbackendrepository.domain.user.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberProfileService {

    private final MemberRepository memberRepository;

    // 추가정보(성별) 업데이트 — 소셜 로그인 최초 가입자 대상
    public void updateProfile(String email, ProfileUpdateRequestDto request) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        member.updateGender(request.gender());
    }
}
