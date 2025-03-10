package hhplus.ecommerce;

import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

@Configuration
class KafkaTestcontainersConfiguration {
    private static final ConfluentKafkaContainer KAFKA_CONTAINER;

    static {
        KAFKA_CONTAINER = new ConfluentKafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.8.1"));
        KAFKA_CONTAINER.start();

        System.setProperty("spring.kafka.bootstrap-servers", KAFKA_CONTAINER.getBootstrapServers());
    }

    @PreDestroy
    public void preDestroy() {
        if (KAFKA_CONTAINER.isRunning()) {
            KAFKA_CONTAINER.stop();
        }
    }
}