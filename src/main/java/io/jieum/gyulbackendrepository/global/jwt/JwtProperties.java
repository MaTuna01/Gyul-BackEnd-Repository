package io.jieum.gyulbackendrepository.global.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret;
    private Long accessExpiration; // application.yml에서 1800000 (30분) 설정을 통해 변경
    private Long refreshExpiration; // application.yml에서 604800000 # 7일 (ms) — Redis TTL과 동일하게 맞출 것
}
