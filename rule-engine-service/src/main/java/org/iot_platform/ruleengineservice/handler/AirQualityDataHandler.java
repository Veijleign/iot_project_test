package org.iot_platform.ruleengineservice.handler;

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iot_platform.protos.air_quality_sensor.AirQualityData;
import org.iot_platform.ruleengineservice.handler.common.SensorDataHandler;
import org.springframework.stereotype.Component;

@Component("air_quality_data")
@Slf4j
@RequiredArgsConstructor
public class AirQualityDataHandler implements SensorDataHandler<AirQualityData> {
    @Override
    public void handle(byte[] data) throws InvalidProtocolBufferException {

    }

    @Override
    public void process(AirQualityData sensorData) {

    }

    @Override
    public String getTopicName() {
        return "air_quality_data";
    }
}
