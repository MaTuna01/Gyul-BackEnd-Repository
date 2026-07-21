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

    // 로그인
    public TokenResponseDto signIn(SignInRequestDto request) {
        // 이메일로 Member 조회 — 보안상 이메일 존재 여부를 노출하지 않도록 비번 불일치와 동일 응답
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));
        // 비밀번호 검증
        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
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
}
