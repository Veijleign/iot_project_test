package org.iot_platform.deviceedgeservice.kafka;

import com.google.protobuf.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
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
