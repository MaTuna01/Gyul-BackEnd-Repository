package io.jieum.gyulbackendrepository;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * 통합 테스트(@SpringBootTest)용 인프라를 Testcontainers로 기동한다.
 *
 * <p>일반 {@code @Configuration}이므로 {@code @SpringBootTest}의 컴포넌트 스캔에 자동 포함된다
 * (개별 테스트에 {@code @Import}를 달 필요 없음). {@code @ServiceConnection}이 datasource/redis/kafka
 * 연결 정보를 컨테이너 값으로 오버라이드하므로, 실제 인프라나 {@code .env} 없이 테스트가 동작한다.
 *
 * <p>이미지는 docker-compose와 동일하게 맞춘다(mysql:8.0, apache/kafka:3.8.0, redis:7-alpine).
 */
@Configuration
public class TestcontainersConfig {

    @Bean
    @ServiceConnection
    MySQLContainer<?> mysqlContainer() {
        return new MySQLContainer<>(DockerImageName.parse("mysql:8.0"));
    }

    @Bean
    @ServiceConnection
    KafkaContainer kafkaContainer() {
        return new KafkaContainer(DockerImageName.parse("apache/kafka:3.8.0"));
    }

    @Bean
    @ServiceConnection(name = "redis")
    GenericContainer<?> redisContainer() {
        return new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                .withExposedPorts(6379);
    }
}
