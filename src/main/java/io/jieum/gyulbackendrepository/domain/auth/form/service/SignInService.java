package io.jieum.gyulbackendrepository.domain.auth.form.service;

import io.jieum.gyulbackendrepository.domain.auth.model.dto.ReissueRequestDto;
import io.jieum.gyulbackendrepository.domain.auth.model.dto.SignInRequestDto;
import io.jieum.gyulbackendrepository.domain.auth.model.dto.TokenResponseDto;
import io.jieum.gyulbackendrepository.domain.user.model.entity.Member;
import io.jieum.gyulbackendrepository.domain.user.repository.MemberRepository;
import io.jieum.gyulbackendrepository.global.exception.BusinessException;
import io.jieum.gyulbackendrepository.global.exception.ErrorCode;
import io.jieum.gyulbackendrepository.global.jwt.JwtProperties;
import io.jieum.gyulbackendrepository.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SignInService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String RT_KEY_PREFIX = "RT:";
    // 로그인 실패 횟수 카운터 (무차별 대입 방어)
    private static final String LOGIN_FAIL_PREFIX = "login-fail:";
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final long LOCK_MINUTES = 10;

    // 로그인
    public TokenResponseDto signIn(SignInRequestDto request) {
        String failKey = LOGIN_FAIL_PREFIX + request.email();
        // 잠금 상태면 자격 검증 이전에 즉시 차단 (계정 기준 무차별 대입 방어)
        if (isLocked(failKey)) {
            throw new BusinessException(ErrorCode.TOO_MANY_LOGIN_ATTEMPTS);
        }
        // 이메일로 Member 조회 — 보안상 이메일 존재 여부를 노출하지 않도록 비번 불일치와 동일 응답
        Member member = memberRepository.findByEmail(request.email()).orElse(null);
        // 자격 불일치(없는 이메일/틀린 비번)는 동일 처리 + 실패 카운트 증가
        if (member == null || !passwordEncoder.matches(request.password(), member.getPassword())) {
            recordFailure(failKey);
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        // 성공 시 실패 카운터 초기화
        redisTemplate.delete(failKey);
        // 토큰 발급
        String accessToken = jwtProvider.generateAccessToken(member);
        String refreshToken = jwtProvider.generateRefreshToken(member.getEmail());
        // Refresh Token → Redis 저장 (TTL: 7일)
        redisTemplate.opsForValue().set(
                RT_KEY_PREFIX + member.getEmail(),
                refreshToken,
                jwtProperties.getRefreshExpiration(),
                TimeUnit.MILLISECONDS
        );
        return new TokenResponseDto(accessToken, refreshToken);
    }

    //Access Token 재발급 로직
    public TokenResponseDto reissue(ReissueRequestDto request) {
        String refreshToken = request.refreshToken();
        //Refresh Token 유효성 검증로직
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        //토큰에서 이메일 추출
        String email = jwtProvider.getEmailFromToken(refreshToken);
        //Redis에 저장된 Refresh Token과 비교 (탈취 감지)
        String savedToken = redisTemplate.opsForValue().get(RT_KEY_PREFIX + email);
        if (savedToken == null || !savedToken.equals(refreshToken)) {
            //탈취가 의심되면 해당 키 삭제 (전체 세션 무효화)
            redisTemplate.delete(RT_KEY_PREFIX + email);
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_MISMATCH);
        }

        //Member 조회후 새로운 토큰 쌍 발급
        Member member = memberRepository.findByEmail(email)
                //조회 예외처리
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        String newAccessToken = jwtProvider.generateAccessToken(member);
        String newRefreshToken = jwtProvider.generateRefreshToken(email);

        //Redis 갱신
        redisTemplate.opsForValue().set(
                RT_KEY_PREFIX + email,
                newRefreshToken,
                jwtProperties.getRefreshExpiration(),
                TimeUnit.MILLISECONDS
        );
        return new TokenResponseDto(newAccessToken, newRefreshToken);
    }

    //로그아웃
    public void signOut(String email) {
        //Redis에서 Refresh Token 삭제
        redisTemplate.delete(RT_KEY_PREFIX + email);
    }

    // 실패 카운터가 임계치 이상이면 잠금 상태
    private boolean isLocked(String failKey) {
        String attempts = redisTemplate.opsForValue().get(failKey);
        return attempts != null && Integer.parseInt(attempts) >= MAX_LOGIN_ATTEMPTS;
    }

    // 실패 1회 기록 — 최초 실패 시 잠금 창(TTL) 설정
    private void recordFailure(String failKey) {
        Long count = redisTemplate.opsForValue().increment(failKey);
        if (count != null && count == 1L) {
            redisTemplate.expire(failKey, LOCK_MINUTES, TimeUnit.MINUTES);
        }
    }
}
