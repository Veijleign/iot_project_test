package org.iot_platform.deviceedgeservice.service;

import com.google.protobuf.MessageLite;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;

@RequiredArgsConstructor
@Slf4j
public class KafkaProducer {

    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    public void sendData(String topic, MessageLite message) {
        byte[] serializedData = message.toByteArray();
        kafkaTemplate.send(topic, serializedData);
        log.info("Sent message to topic [{}]: {}", topic, message.getClass().getSimpleName());
    }

}
