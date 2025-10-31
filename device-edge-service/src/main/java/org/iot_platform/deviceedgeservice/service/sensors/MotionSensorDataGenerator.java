package org.iot_platform.deviceedgeservice.service.sensors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iot_platform.protos.motion_sensor_data.*;
import org.iot_platform.protos.sensor_common.SensorHeader;
import org.iot_platform.protos.sensor_common.SignalStrength;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class MotionSensorDataGenerator implements SensorDataGenerator<MotionSensorData> {

    private final Random random = new Random();

    public MotionSensorData generateData() {
        SensorHeader header = SensorHeader.newBuilder()
                .setDeviceId(UUID.randomUUID().toString())
                .setDeviceType("MOTION_SENSOR")
                .setTimestamp(System.currentTimeMillis())
                .setHotelId("HOTEL_" + (random.nextInt(5) + 1))
                .setRoomNumber(String.valueOf(random.nextInt(100) + 1))
                .setBatteryLevel(75.0f + random.nextFloat() * 25.0f)
                .setSignalStrength(getRandomSignalStrength())
                .build();

        boolean motionDetected = random.nextDouble() < 0.3; // 30% chance of motion
        int detectionCount = motionDetected ? random.nextInt(10) + 1 : 0;
        float detectionConfidence = motionDetected ? 0.7f + random.nextFloat() * 0.3f : 0.0f;

        // Generate motion events
        List<MotionEvent> events = new ArrayList<>();
        if (motionDetected) {
            String[] zones = {"entrance", "bed", "bathroom", "desk"};
            for (int i = 0; i < detectionCount; i++) {
                MotionEvent event = MotionEvent.newBuilder()
                        .setEventTimestamp(System.currentTimeMillis() - random.nextInt(60000))
                        .setMotionType(MotionType.MOVEMENT_DETECTED)
                        .setZone(zones[random.nextInt(zones.length)])
                        .build();
                events.add(event);
            }
        }

        RoomStatus roomStatus = motionDetected ? RoomStatus.ROOM_OCCUPIED :
                (random.nextDouble() < 0.1 ? RoomStatus.ROOM_CLEANING_NEEDED : RoomStatus.ROOM_EMPTY);

        return MotionSensorData.newBuilder()
                .setHeader(header)
                .setMotionDetected(motionDetected)
                .setDetectionCount(detectionCount)
                .setDetectionConfidence(detectionConfidence)
                .addAllEvents(events)
                .setRoomStatus(roomStatus)
                .build();
    }

    private SignalStrength getRandomSignalStrength() {
        List<SignalStrength> values = Arrays.stream(SignalStrength.values())
                .filter(level -> level != SignalStrength.UNRECOGNIZED)
                .toList();
        return values.get(random.nextInt(values.size()));
    }
}