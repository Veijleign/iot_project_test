package org.iot_platform.deviceedgeservice.service.sensors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iot_platform.protos.water_leak_data.*;
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
public class WaterLeakDataGenerator implements SensorDataGenerator<WaterLeakData> {

    private final Random random = new Random();

    public WaterLeakData generateData() {
        SensorHeader header = SensorHeader.newBuilder()
                .setDeviceId(UUID.randomUUID().toString())
                .setDeviceType("WATER_LEAK_SENSOR")
                .setTimestamp(System.currentTimeMillis())
                .setHotelId("HOTEL_" + (random.nextInt(5) + 1))
                .setRoomNumber(String.valueOf(random.nextInt(100) + 1))
                .setBatteryLevel(90.0f + random.nextFloat() * 10.0f)
                .setSignalStrength(getRandomSignalStrength())
                .build();

        // 5% chance of leak for testing
        boolean leakDetected = random.nextDouble() < 0.05;

        String[] locations = {"bathroom", "kitchen", "corridor", "laundry"};
        String location = locations[random.nextInt(locations.length)];

        LeakSeverity severity;
        if (leakDetected) {
            LeakSeverity[] severities = {LeakSeverity.MINOR, LeakSeverity.MODERATE, LeakSeverity.SEVERE, LeakSeverity.CRITICAL};
            severity = severities[random.nextInt(severities.length)];
        } else {
            severity = LeakSeverity.NO_LEAK;
        }

        List<String> affectedZones = Arrays.asList(location);
        if (severity == LeakSeverity.SEVERE || severity == LeakSeverity.CRITICAL) {
            affectedZones = Arrays.asList(location, "corridor", "adjacent_room");
        }

        return WaterLeakData.newBuilder()
                .setHeader(header)
                .setLeakDetected(leakDetected)
                .setSeverity(severity)
                .setLocation(location)
                .setDurationSeconds(leakDetected ? random.nextInt(3600) : 0)
                .setDetectionConfidence(leakDetected ? 0.8f + random.nextFloat() * 0.2f : 0.0f)
                .addAllAffectedZones(affectedZones)
                .build();
    }

    private SignalStrength getRandomSignalStrength() {
        List<SignalStrength> values = Arrays.stream(SignalStrength.values())
                .filter(level -> level != SignalStrength.UNRECOGNIZED)
                .toList();
        return values.get(random.nextInt(values.size()));
    }
}