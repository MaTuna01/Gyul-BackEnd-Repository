package io.jieum.gyulbackendrepository.domain.user.model.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Builder
    public Member(String email, String password, String name, LocalDateTime createdAt, Gender gender, Role role) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.createdAt = createdAt;
        this.gender = gender;
        this.role = role;
    }
}
