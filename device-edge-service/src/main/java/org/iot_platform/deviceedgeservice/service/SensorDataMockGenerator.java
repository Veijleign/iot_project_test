package org.iot_platform.deviceedgeservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iot_platform.deviceedgeservice.service.sensors.AirQualityDataGenerator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@EnableScheduling
@ConditionalOnProperty(prefix = "iot.mock", name = "enabled", havingValue = "true")
public class SensorDataMockGenerator {

    private final AirQualityDataGenerator airQualityGenerator;
    private final ClimateDataGenerator climateGenerator;
    private final EnergyMeterDataGenerator energyGenerator;
    private final MotionSensorDataGenerator motionGenerator;
    private final WaterLeakDataGenerator waterLeakGenerator;
    private final WaterMeterDataGenerator waterMeterGenerator;
    private final KafkaProducer producerService;

    private final List<String> roomNumbers = Arrays.asList("101", "102", "103", "201", "202", "301");


    @Async
    @Scheduled(fixedRate = 30000)
    public void generateClimateData() {
        try {
            var data = climateGenerator.generate();
            producerService.sendData("climate_data", data.getHeader().getRoomNumber(), data);
            log.debug("Generated climate data for room: {}", data.getHeader().getRoomNumber());
        } catch (Exception e) {
            log.error("Error generating climate data", e);
        }
    }

    @Async
    @Scheduled(fixedRate = 30000)
    public void generateAirQualityData() {
        try {
            var data = airQualityGenerator.generateAirQualityData();
            producerService.sendData("climate_data", data.getHeader().getRoomNumber(), data);
            log.debug("Generated climate data for room: {}", data.getHeader().getRoomNumber());
        } catch (Exception e) {
            log.error("Error generating climate data", e);
        }
    }


}
