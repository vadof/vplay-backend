package com.vcasino.common.kafka.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

public class ConsumerConfigs {

    private static Map<String, Object> commonConsumerConfig(String groupId, String bootstrapServers) {
        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.vcasino.common.kafka.event");

        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return props;
    }

    public static Map<String, Object> consumerManualAckConfig(String groupId, String bootstrapServers) {
        Map<String, Object> props = commonConsumerConfig(groupId, bootstrapServers);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        return props;
    }

    public static Map<String, Object> consumerBatchConfig(String groupId, String bootstrapServers) {
        Map<String, Object> props = commonConsumerConfig(groupId, bootstrapServers);

        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1000);
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1024); // 1KB
        props.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, 52428800); // 50MB
        props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, 10485760); // 10MB per partition
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 45000); // 45 sec

        return props;
    }

    public static KafkaListenerContainerFactory<
            ConcurrentMessageListenerContainer<String, Object>> buildKafkaListenerContainerFactory(
            String groupId, String bootstrapServers
    ) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        Map<String, Object> config = consumerManualAckConfig(groupId, bootstrapServers);

        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(config));
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);

        return factory;
    }

    public static KafkaListenerContainerFactory<
            ConcurrentMessageListenerContainer<String, Object>> buildKafkaListenerBatchFactory(
            String groupId, String bootstrapServers
    ) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        Map<String, Object> config = consumerBatchConfig(groupId, bootstrapServers);

        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(config));
        factory.setBatchListener(true);

        return factory;
    }

}
