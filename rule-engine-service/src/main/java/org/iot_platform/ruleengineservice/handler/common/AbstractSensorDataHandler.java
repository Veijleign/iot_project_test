package org.iot_platform.ruleengineservice.handler.common;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractSensorDataHandler<T extends Message> implements SensorDataHandler<T> {

    @Override
    public void handle(byte[] data) throws InvalidProtocolBufferException {
        try {
            T sensorData = deserialize(data);

            if (!validate(sensorData)) {
                log.warn("Invalid sensor data received for topic: {}", getTopicName());
                return;
            }

            log.debug("Processing {} data: {}", getTopicName(), sensorData);

            process(sensorData);

        } catch (InvalidProtocolBufferException e) {
            log.error("Failed to deserialize data from topic: {}", getTopicName(), e);
            throw e;
        } catch (Exception e) {
            log.error("Error processing data from topic: {}", getTopicName(), e);
            throw new RuntimeException("Processing failed", e);
        }
    }

    /**
     * Десериализует byte[] в конкретный Protobuf тип
     * Реализуется в каждом handler
     */
    protected abstract T deserialize(byte[] data) throws InvalidProtocolBufferException;

    protected boolean validate(T sensorData) {
        return true;  // Можно переопределить в конкретных handlers
    }
}
