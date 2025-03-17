package com.vcasino.common.kafka.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

public class ProducerConfigs {

    public static Map<String, Object> commonConfig(String producerId, String bootstrapServers,
                                                   String acks, int retries, boolean enableIdempotence) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.CLIENT_ID_CONFIG, producerId);

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        props.put(ProducerConfig.ACKS_CONFIG, acks);
        props.put(ProducerConfig.RETRIES_CONFIG, retries);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, enableIdempotence);

        return props;
    }

    public static Map<String, Object> atLeastOnceConfig(String producerId, String bootstrapServers) {
        return commonConfig(producerId, bootstrapServers, "all", 3, true);
    }

    public static Map<String, Object> atMostOnceConfig(String producerId, String bootstrapServers) {
        return commonConfig(producerId, bootstrapServers, "0", 0, false);
    }

    public static KafkaTemplate<String, Object> buildKafkaTemplate(Map<String, Object> config) {
        ProducerFactory<String, Object> producerFactory = new DefaultKafkaProducerFactory<>(config);
        return new KafkaTemplate<>(producerFactory);
    }

    public static KafkaTemplate<String, Object> buildAtLeastOnceKafkaTemplate(String producerId, String bootstrapServers) {
        return buildKafkaTemplate(atLeastOnceConfig(producerId, bootstrapServers));
    }

    public static KafkaTemplate<String, Object> buildAtMostOnceKafkaTemplate(String producerId, String bootstrapServers) {
        return buildKafkaTemplate(atMostOnceConfig(producerId, bootstrapServers));
    }

}
