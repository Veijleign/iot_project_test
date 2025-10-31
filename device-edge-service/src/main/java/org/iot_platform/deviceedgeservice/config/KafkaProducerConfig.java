package org.iot_platform.deviceedgeservice.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.ProducerListener;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, byte[]> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);

        // Performance settings
        props.put(ProducerConfig.ACKS_CONFIG, "all"); // Ждем подтверждения от лидера
        props.put(ProducerConfig.RETRIES_CONFIG, 5);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 10); // Ждем 10мс для батчинга
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432L); // 32MB
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy"); // Сжатие
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        // Timeouts
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 60000);
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 60000);

        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, byte[]> kafkaTemplate() {
        KafkaTemplate<String, byte[]> template = new KafkaTemplate<>(producerFactory());

        template.setProducerListener(new ProducerListener<String, byte[]>() {
            @Override
            public void onSuccess(ProducerRecord<String, byte[]> producerRecord, RecordMetadata recordMetadata) {
                log.info("Message sent successfully to topic: {}, partition: {}, offset: {}",
                        producerRecord.topic(), recordMetadata.partition(), recordMetadata.offset());
            }

            @Override
            public void onError(ProducerRecord<String, byte[]> producerRecord, RecordMetadata recordMetadata, Exception exception) {
                log.error("Failed to send message to topic: {}, error: {}",
                        producerRecord.topic(), exception.getMessage());
            }
        });
        return template;
    }
}
