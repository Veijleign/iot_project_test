package org.iot_platform.deviceedgeservice.service;

import com.google.common.util.concurrent.ListenableFuture;
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

    public void sendData(String topic, String key, MessageLite protobufMessage) {

        byte[] serializedData = protobufMessage.toByteArray();

        ListenableFuture<SendResult<String, byte[]>> sendResult =
                kafkaTemplate.send(topic, key, serializedData);



        log.info("Sent message to topic [{}]: {}", topic, protobufMessage.getClass().getSimpleName());
    }

}
