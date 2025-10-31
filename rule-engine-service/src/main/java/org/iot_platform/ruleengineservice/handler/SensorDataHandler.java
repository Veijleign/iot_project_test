package org.iot_platform.ruleengineservice.handler;

import com.google.protobuf.InvalidProtocolBufferException;

public interface SensorDataHandler<T> {

    /**
     * Process raw byte[] data
     */
    void handle(byte[] data) throws InvalidProtocolBufferException;

    /**
     * Process deserialized data
     */
    void process(T sensorData);

    /**
     * topic name, which processes data
     */
    String getTopicName();
}
