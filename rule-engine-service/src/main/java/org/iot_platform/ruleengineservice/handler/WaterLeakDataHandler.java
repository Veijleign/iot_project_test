package org.iot_platform.ruleengineservice.handler;

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iot_platform.protos.water_leak_data.WaterLeakData;
import org.iot_platform.ruleengineservice.handler.common.SensorDataHandler;
import org.springframework.stereotype.Component;

@Component("water_leak_data")
@Slf4j
@RequiredArgsConstructor
public class WaterLeakDataHandler implements SensorDataHandler<WaterLeakData> {
    @Override
    public void handle(byte[] data) throws InvalidProtocolBufferException {

    }

    @Override
    public void process(WaterLeakData sensorData) {

    }

    @Override
    public String getTopicName() {
        return "water_leak_data";
    }
}
