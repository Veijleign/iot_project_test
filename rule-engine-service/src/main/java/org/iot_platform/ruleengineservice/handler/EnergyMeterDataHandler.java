package org.iot_platform.ruleengineservice.handler;

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iot_platform.protos.energy_meter_data.EnergyMeterData;
import org.iot_platform.ruleengineservice.handler.common.SensorDataHandler;
import org.springframework.stereotype.Component;

@Component("energy_data")
@Slf4j
@RequiredArgsConstructor
public class EnergyMeterDataHandler implements SensorDataHandler<EnergyMeterData> {
    @Override
    public void handle(byte[] data) throws InvalidProtocolBufferException {

    }

    @Override
    public void process(EnergyMeterData sensorData) {

    }

    @Override
    public String getTopicName() {
        return "energy_data";
    }
}
