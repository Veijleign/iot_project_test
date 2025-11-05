package org.iot_platform.ruleengineservice.handler;

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iot_platform.protos.water_meter_data.WaterMeterData;
import org.iot_platform.ruleengineservice.handler.common.SensorDataHandler;
import org.springframework.stereotype.Component;

@Component("water_meter_data")
@Slf4j
@RequiredArgsConstructor
public class WaterMeterDataHandler implements SensorDataHandler<WaterMeterData> {
    @Override
    public void handle(byte[] data) throws InvalidProtocolBufferException {

    }

    @Override
    public void process(WaterMeterData sensorData) {

    }

    @Override
    public String getTopicName() {
        return "water_meter_data";
    }
}
