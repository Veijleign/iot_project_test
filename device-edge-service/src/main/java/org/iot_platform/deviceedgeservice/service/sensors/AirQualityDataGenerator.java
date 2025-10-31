package org.iot_platform.deviceedgeservice.service.sensors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iot_platform.protos.air_quality_sensor.*;
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
public class AirQualityDataGenerator implements SensorDataGenerator<AirQualityData> {

    private final Random random = new Random();

    private SignalStrength getRandomSignalStrength() {
        List<SignalStrength> values = Arrays.stream(SignalStrength.values())
                .filter(level -> level != SignalStrength.UNRECOGNIZED)
                .toList();
        return values.get(random.nextInt(values.size()));
    }

    private AirQualityLevel getRandomAirQualityLevel() {
        List<AirQualityLevel> values = Arrays.stream(AirQualityLevel.values())
                .filter(level -> level != AirQualityLevel.UNRECOGNIZED)
                .toList();
        return values.get(random.nextInt(values.size()));
    }

    private AlertSeverity getRandomAlertSeverity() {
        List<AlertSeverity> values = Arrays.stream(AlertSeverity.values())
                .filter(level -> level != AlertSeverity.UNRECOGNIZED)
                .toList();
        return values.get(random.nextInt(values.size()));
    }

    public AirQualityData generateData() {
        SensorHeader header = SensorHeader.newBuilder()
                .setDeviceId(UUID.randomUUID().toString())
                .setDeviceType("CLIMATE_SENSOR")
                .setTimestamp(System.currentTimeMillis())
                .setHotelId("HOTEL_" + (random.nextInt(5) + 1))
                .setRoomNumber(String.valueOf(random.nextInt(100) + 1))
                .setBatteryLevel(80.0f + random.nextFloat() * 20.0f)
                .setSignalStrength(getRandomSignalStrength())
                .build();

        VentilationRecommendation ventilationRecommendation = VentilationRecommendation.newBuilder()
                .setVentilationNeeded(true)
                .setRecommendedDurationMin(random.nextInt(5) + 1)
                .setCurrentLevel(getRandomAirQualityLevel())
                .setTargetLevel(getRandomAirQualityLevel())
                .build();

        String[] myArray = {"co2", "tvoc", "pm2_5"};
        String[] myRecommendations = {"Ventilation needed", "Everything is ok", "Too much smoke!"};

        AirQualityAlert airQualityAlert = AirQualityAlert.newBuilder()
                .setSeverity(getRandomAlertSeverity())
                .setParameter(myArray[random.nextInt(myArray.length)])
                .setCurrentValue(10.0f + random.nextFloat() * 5.0f)
                .setThreshold(10.0f + random.nextFloat() * 5.0f)
                .setRecommendation(myRecommendations[random.nextInt(myRecommendations.length)])
                .build();

        AirQualityData airData = AirQualityData.newBuilder()
                .setHeader(header)
                .setCo2Ppm(random.nextInt(90) + 5)
                .setTvocPpb(12.0f + random.nextFloat() * 4.0f)
                .setPm25(10.0f + random.nextFloat() * 5.0f)
                .setPm10(7.0f + random.nextFloat() * 3.0f)
                .setOxygenPercent(80.0f + random.nextFloat() * 20.0f)
                .setAirQualityIndex(random.nextInt(90) + 5)
                .setVentilation(ventilationRecommendation)
                .addAlerts(airQualityAlert) // ??? array?
                .build();

        return airData;
    }
}
