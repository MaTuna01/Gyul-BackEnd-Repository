package io.jieum.gyulbackendrepository.domain.user.model.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "MEMBER")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id", unique = true, nullable = false)
    private Long id;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "name")
    private String name;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 성별의 경우 nullable 처리를 어떻게 할지, 그냥 문자열로 받을지, enum으로 정의해서 받을지 논의 필요
    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private Role role;

    // 소셜 로그인 제공자 (google, kakao). Form 전용 회원은 null
    @Column(name = "provider")
    private String provider;

    // 제공자 내 사용자 고유 ID
    @Column(name = "provider_id")
    private String providerId;

    @Builder
    public Member(String email, String password, String name, LocalDateTime createdAt, Gender gender, Role role,
                  String provider, String providerId) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.createdAt = createdAt;
        this.gender = gender;
        this.role = role;
        this.provider = provider;
        this.providerId = providerId;
    }

    // 소셜 로그인 최초 가입 시 추가정보(성별) 입력
    public void updateGender(Gender gender) {
        this.gender = gender;
    }

    // 기존(Form) 계정에 소셜 로그인 정보 연결 — 이메일 기준 계정 병합
    public void linkOAuth(String provider, String providerId) {
        this.provider = provider;
        this.providerId = providerId;
    }
}
