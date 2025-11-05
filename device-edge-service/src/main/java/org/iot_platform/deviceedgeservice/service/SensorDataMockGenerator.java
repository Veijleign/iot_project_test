package org.iot_platform.deviceedgeservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iot_platform.deviceedgeservice.kafka.KafkaProducer;
import org.iot_platform.deviceedgeservice.service.sensors.*;
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
//@ConditionalOnProperty(prefix = "iot.mock", name = "enabled", havingValue = "true")
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
    @Scheduled(fixedRate = 1500)
    public void generateClimateData() {
        try {
            var data = climateGenerator.generateData();
            producerService.sendData("climate_data", data.getHeader().getRoomNumber(), data);
            log.debug("Generated climate data for room: {}", data.getHeader().getRoomNumber());
        } catch (Exception e) {
            log.error("Error generating climate data", e.getStackTrace());
        }
    }

    @Async
    @Scheduled(fixedRate = 2500)
    public void generateAirQualityData() {
        try {
            var data = airQualityGenerator.generateData();
            producerService.sendData("air_quality_data", data.getHeader().getRoomNumber(), data);
            log.debug("Generated air quality data for room: {}", data.getHeader().getRoomNumber());
        } catch (Exception e) {
            log.error("Error generating air quality data", e);
        }
    }

    @Async
    @Scheduled(fixedRate = 4000)
    public void generateMotionData() {
        try {
            var data = motionGenerator.generateData();
            producerService.sendData("motion_data", data.getHeader().getRoomNumber(), data);
            log.debug("Generated motion data for room: {}", data.getHeader().getRoomNumber());
        } catch (Exception e) {
            log.error("Error generating motion data", e);
        }
    }

    @Async
    @Scheduled(fixedRate = 3000)
    public void generateEnergyData() {
        try {
            var data = energyGenerator.generateData();
            producerService.sendData("energy_data", data.getHeader().getRoomNumber(), data);
            log.debug("Generated energy data for room: {}", data.getHeader().getRoomNumber());
        } catch (Exception e) {
            log.error("Error generating energy data", e);
        }
    }

    @Async
    @Scheduled(fixedRate = 3500)
    public void generateWaterMeterData() {
        try {
            var data = waterMeterGenerator.generateData();
            producerService.sendData("water_meter_data", data.getHeader().getRoomNumber(), data);
            log.debug("Generated water meter data for room: {}", data.getHeader().getRoomNumber());
        } catch (Exception e) {
            log.error("Error generating water meter data", e);
        }
    }

    @Async
    @Scheduled(fixedRate = 2000)
    public void generateWaterLeakData() {
        try {
            var data = waterLeakGenerator.generateData();
            // Отправляем только если обнаружена протечка
            if (data.getLeakDetected()) {
                producerService.sendData("water_leak_data", data.getHeader().getRoomNumber(), data);
                log.warn("Generated water leak alert for room: {}", data.getHeader().getRoomNumber());
            }
        } catch (Exception e) {
            log.error("Error generating water leak data", e);
        }
    }
}
