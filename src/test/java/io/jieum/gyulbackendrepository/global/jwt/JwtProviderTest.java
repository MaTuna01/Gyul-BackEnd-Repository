package io.jieum.gyulbackendrepository.global.jwt;

import io.jieum.gyulbackendrepository.domain.user.model.entity.Member;
import io.jieum.gyulbackendrepository.domain.user.model.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

// 스프링 컨텍스트 없이 Access Token claim 발급/추출을 검증
class JwtProviderTest {

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        // HS256 최소 길이(32바이트) 이상 시크릿
        properties.setSecret("test-secret-key-for-jwt-provider-unit-test-1234567890");
        properties.setAccessExpiration(1_800_000L);
        properties.setRefreshExpiration(604_800_000L);
        jwtProvider = new JwtProvider(properties);
    }

    private Member member() {
        Member member = Member.builder()
                .email("user@example.com")
                .role(Role.MEMBER)
                .build();
        // id는 @GeneratedValue라 빌더로 못 넣으므로 리플렉션으로 주입
        ReflectionTestUtils.setField(member, "id", 42L);
        return member;
    }

    @Test
    void Access_Token에_memberId_claim이_담긴다() {
        String token = jwtProvider.generateAccessToken(member());

        assertThat(jwtProvider.getMemberIdFromToken(token)).isEqualTo(42L);
    }

    @Test
    void Access_Token에서_이메일을_추출한다() {
        String token = jwtProvider.generateAccessToken(member());

        assertThat(jwtProvider.getEmailFromToken(token)).isEqualTo("user@example.com");
    }

    @Test
    void memberId_claim이_없는_Refresh_Token은_null을_반환한다() {
        String refreshToken = jwtProvider.generateRefreshToken("user@example.com");

        assertThat(jwtProvider.getMemberIdFromToken(refreshToken)).isNull();
    }
}
