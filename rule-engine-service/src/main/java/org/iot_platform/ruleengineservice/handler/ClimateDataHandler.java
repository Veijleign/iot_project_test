package org.iot_platform.ruleengineservice.handler;

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iot_platform.protos.climate_data.ClimateData;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Slf4j
@RequiredArgsConstructor
public class ClimateDataHandler implements SensorDataHandler<ClimateData> {
    @Override
    public void handle(byte[] data) throws InvalidProtocolBufferException {
        log.info("---Received ClimateData---");
    }

    @Override
    public void process(ClimateData sensorData) {

    }

    @Override
    public String getTopicName() {
        return "";
    }
}
