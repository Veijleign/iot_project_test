package org.iot_platform.ruleengineservice.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iot_platform.ruleengineservice.handler.SensorDataHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@Component
public class SensorDataConsumer {

    private final Map<String, SensorDataHandler<?>> handlers;

    @KafkaListener(
            topics = {
                    "climate_data",
                    "air_quality_data",
                    "motion_data",
                    "energy_data",
                    "water_leak_data",
                    "water_meter_data"
            },
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeSensorData(
            @Payload byte[] data,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            Acknowledgment acknowledgment
    ) {
        try {
            log.debug("Received message from topic: {}, key: {}", topic, key);

            SensorDataHandler<?> handler = handlers.get(topic);

            if (handler == null) {
                log.warn("No handler found for topic: {}", topic);
                // Даже если обработчик не найден, нужно подтвердить сообщение,
                // иначе consumer будет застревать на этом сообщении.
                if (acknowledgment != null) {
                    acknowledgment.acknowledge();
                }
                return;
            }

            handler.handle(data);
            // Если обработка прошла успешно, подтверждаем сообщение
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
        } catch (Exception e) {
            log.error("Error processing message from topic: {}", topic, e);
        }
    }
}
