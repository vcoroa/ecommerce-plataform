package br.com.vcoroa.ecommerce.platform.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic orderPaidTopic() {
        return TopicBuilder.name("order.paid")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
