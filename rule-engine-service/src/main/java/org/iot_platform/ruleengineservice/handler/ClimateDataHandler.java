package org.iot_platform.ruleengineservice.handler;

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iot_platform.protos.climate_data.ClimateData;
import org.iot_platform.ruleengineservice.handler.common.AbstractSensorDataHandler;
import org.springframework.stereotype.Component;

@Component("climate_data")
@Slf4j
@RequiredArgsConstructor
public class ClimateDataHandler extends AbstractSensorDataHandler<ClimateData> {

    @Override
    protected ClimateData deserialize(byte[] data) throws InvalidProtocolBufferException {
        return ClimateData.parseFrom(data);
    }

    @Override
    public void process(ClimateData sensorData) {
        log.info("Processing climate data: deviceId={}, room={}, temp={}°C, humidity={}%",
                sensorData.getHeader().getDeviceId(),
                sensorData.getHeader().getRoomNumber(),
                sensorData.getTemperatureC(),
                sensorData.getHumidityPercent());
    }

    @Override
    public String getTopicName() {
        return "climate_data";
    }

    @Override
    protected boolean validate(ClimateData sensorData) {
        float temp = sensorData.getTemperatureC();
        float humidity = sensorData.getHumidityPercent();

        if (temp < -50 || temp > 80) {
            log.warn("Invalid temperature: {}°C", temp);
            return false;
        }

        if (humidity < 0 || humidity > 100) {
            log.warn("Invalid humidity: {}%", humidity);
            return false;
        }
        return true;
    }
}
