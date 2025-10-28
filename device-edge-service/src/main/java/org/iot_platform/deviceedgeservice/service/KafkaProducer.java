package org.iot_platform.deviceedgeservice.service;

import com.google.protobuf.Message;
import com.google.protobuf.MessageLite;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Slf4j
public class KafkaProducer {

    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    public <T extends Message> void sendData(String topic, String key, T protobufMessage) {
        byte[] serializedData = protobufMessage.toByteArray();

        kafkaTemplate.send(topic, key, serializedData)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send message to topic: {}, error: {}", topic, ex.getMessage());
                    } else {
                        log.info("Sent to topic: {}, partition: {}, offset: {}",
                                topic,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset()
                        );
                    }
                });
    }

}
