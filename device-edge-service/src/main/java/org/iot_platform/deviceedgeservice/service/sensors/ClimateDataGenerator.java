package org.iot_platform.deviceedgeservice.service.sensors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iot_platform.protos.climate_data.*;
import org.iot_platform.protos.sensor_common.SensorHeader;
import org.iot_platform.protos.sensor_common.SignalStrength;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClimateDataGenerator implements SensorDataGenerator<ClimateData> {

    private final Random random = new Random();

    public ClimateData generateData() {
        SensorHeader header = SensorHeader.newBuilder()
                .setDeviceId(UUID.randomUUID().toString())
                .setDeviceType("CLIMATE_SENSOR")
                .setTimestamp(System.currentTimeMillis())
                .setHotelId("HOTEL_" + (random.nextInt(5) + 1))
                .setRoomNumber(String.valueOf(random.nextInt(100) + 1))
                .setBatteryLevel(80.0f + random.nextFloat() * 20.0f)
                .setSignalStrength(getRandomSignalStrength())
                .build();

        float temperature = 18.0f + random.nextFloat() * 12.0f; // 18-30°C
        float humidity = 30.0f + random.nextFloat() * 50.0f;    // 30-80%

        ComfortLevel comfortLevel = calculateComfortLevel(temperature, humidity);

        return ClimateData.newBuilder()
                .setHeader(header)
                .setTemperatureC(temperature)
                .setHumidityPercent(humidity)
                .setHeatIndex(calculateHeatIndex(temperature, humidity))
                .setDewPoint(calculateDewPoint(temperature, humidity))
                .setComfortLevel(comfortLevel)
                .setHeatingRequired(temperature < 20.0f)
                .setCoolingRequired(temperature > 25.0f)
                .setHumidificationRequired(humidity < 40.0f)
                .setDehumidificationRequired(humidity > 60.0f)
                .build();
    }

    private ComfortLevel calculateComfortLevel(float temp, float humidity) {
        if (temp >= 20 && temp <= 24 && humidity >= 40 && humidity <= 60) {
            return ComfortLevel.COMFORTABLE;
        } else if ((temp >= 18 && temp < 20) || (temp > 24 && temp <= 26) ||
                (humidity >= 35 && humidity < 40) || (humidity > 60 && humidity <= 65)) {
            return ComfortLevel.SLIGHTLY_UNCOMFORTABLE;
        } else if (temp < 15 || temp > 30 || humidity < 30 || humidity > 70) {
            return ComfortLevel.DANGEROUS;
        } else {
            return ComfortLevel.UNCOMFORTABLE;
        }
    }

    private float calculateHeatIndex(float temp, float humidity) {
        return temp + 0.05f * humidity;
    }

    private float calculateDewPoint(float temp, float humidity) {
        return temp - (100 - humidity) / 5;
    }

    private SignalStrength getRandomSignalStrength() {
        List<SignalStrength> values = Arrays.stream(SignalStrength.values())
                .filter(level -> level != SignalStrength.UNRECOGNIZED)
                .toList();
        return values.get(random.nextInt(values.size()));
    }
}