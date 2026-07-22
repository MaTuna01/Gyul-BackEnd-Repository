package io.jieum.gyulbackendrepository.global.config;

import io.jieum.gyulbackendrepository.domain.report.messaging.ReportMessageParseException;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

/**
 * 분석 리포트 컨슈머의 예외 처리/재처리/DLQ 정책 (docs/integration-spec.md §3.3).
 *
 * <ul>
 *   <li>스키마 파싱 실패({@link ReportMessageParseException}): 재처리 무의미 → 재시도 없이 즉시 DLQ</li>
 *   <li>그 외 일시적 오류(DB 순단 등): 1초 간격 2회 재시도 후 DLQ</li>
 * </ul>
 *
 * 여기 정의한 {@link DefaultErrorHandler}(= {@code CommonErrorHandler})가 유일한 빈이므로,
 * Spring Boot가 자동 구성한 Kafka 리스너 컨테이너 팩토리에 자동으로 연결된다.
 */
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${app.kafka.topic.analysis-report-dlq}")
    private String dlqTopic;

    // DLQ 발행 전용 Producer (원본 메시지가 String이므로 String 직렬화)
    @Bean
    public ProducerFactory<String, String> dlqProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, String> dlqKafkaTemplate() {
        return new KafkaTemplate<>(dlqProducerFactory());
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, String> dlqKafkaTemplate) {
        // 실패 레코드를 DLQ 토픽으로 발행 (파티션은 -1로 브로커에 위임 — DLQ 파티션 수가 달라도 안전)
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                dlqKafkaTemplate,
                (record, exception) -> new TopicPartition(dlqTopic, -1)
        );

        // 일시적 오류: 1초 간격 2회 재시도(총 3회) 후 DLQ
        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 2L));
        // 파싱 실패는 재시도 없이 곧바로 DLQ
        handler.addNotRetryableExceptions(ReportMessageParseException.class);
        return handler;
    }
}
