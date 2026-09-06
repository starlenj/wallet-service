package com.nasuh.walletservice.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
  public static final String TRANSFER_COMPLETED_TOPIC = "wallet.transfer.completed";

  @Bean
  public NewTopic transferCompledTopic() {
    return TopicBuilder.name(TRANSFER_COMPLETED_TOPIC).partitions(3).replicas(1).build();
  }
}
