package org.iot_platform.ruleengineservice.handler;

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iot_platform.protos.motion_sensor_data.MotionSensorData;
import org.iot_platform.ruleengineservice.handler.common.SensorDataHandler;
import org.springframework.stereotype.Component;

@Component("motion_data")
@Slf4j
@RequiredArgsConstructor
public class MotionSensorDataHandler implements SensorDataHandler<MotionSensorData> {
    @Override
    public void handle(byte[] data) throws InvalidProtocolBufferException {

    }

    @Override
    public void process(MotionSensorData sensorData) {

    }

    @Override
    public String getTopicName() {
        return "motion_data";
    }
}
