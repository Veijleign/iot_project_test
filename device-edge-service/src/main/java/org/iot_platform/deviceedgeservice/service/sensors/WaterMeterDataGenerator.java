package org.iot_platform.deviceedgeservice.service.sensors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iot_platform.protos.water_meter_data.*;
import org.iot_platform.protos.sensor_common.SensorHeader;
import org.iot_platform.protos.sensor_common.SignalStrength;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class WaterMeterDataGenerator implements SensorDataGenerator<WaterMeterData> {

    private final Random random = new Random();
    private final Map<String, Double> totalConsumptionMap = new HashMap<>();

    public WaterMeterData generateData() {
        String roomNumber = String.valueOf(random.nextInt(100) + 1);
        String hotelId = "HOTEL_" + (random.nextInt(5) + 1);

        SensorHeader header = SensorHeader.newBuilder()
                .setDeviceId(UUID.randomUUID().toString())
                .setDeviceType("WATER_METER")
                .setTimestamp(System.currentTimeMillis())
                .setHotelId(hotelId)
                .setRoomNumber(roomNumber)
                .setBatteryLevel(88.0f + random.nextFloat() * 12.0f)
                .setSignalStrength(getRandomSignalStrength())
                .build();

        String waterKey = hotelId + "_" + roomNumber;
        double baseConsumption = totalConsumptionMap.getOrDefault(waterKey, 0.0);
        double hourlyConsumption = 5.0 + random.nextDouble() * 50.0; // 5-55 liters
        double totalConsumption = baseConsumption + hourlyConsumption;
        totalConsumptionMap.put(waterKey, totalConsumption);

        // Generate usage events
        List<WaterUsageEvent> usageEvents = new ArrayList<>();
        int eventCount = random.nextInt(5);
        for (int i = 0; i < eventCount; i++) {
            UsageType[] usageTypes = {UsageType.SHOWER, UsageType.FAUCET, UsageType.TOILET, UsageType.BATH};
            WaterUsageEvent event = WaterUsageEvent.newBuilder()
                    .setStartTime(System.currentTimeMillis() - random.nextInt(3600000))
                    .setEndTime(System.currentTimeMillis() - random.nextInt(1800000))
                    .setVolumeL(2.0 + random.nextDouble() * 15.0)
                    .setType(usageTypes[random.nextInt(usageTypes.length)])
                    .build();
            usageEvents.add(event);
        }

        LeakDetection leakDetection = LeakDetection.newBuilder()
                .setPossibleLeak(random.nextDouble() < 0.02) // 2% chance of possible leak
                .setLeakRateLph(random.nextDouble() * 2.0)
                .setSuspectedLocation("bathroom")
                .build();

        WaterQuality waterQuality = WaterQuality.newBuilder()
                .setTemperatureC(15.0f + random.nextFloat() * 15.0f)
                .setPhLevel(6.5f + random.nextFloat() * 1.5f)
                .setTurbidity(random.nextFloat() * 1.0f)
                .build();

        return WaterMeterData.newBuilder()
                .setHeader(header)
                .setTotalConsumptionL(totalConsumption)
                .setFlowRateLpm(random.nextDouble() * 3.0)
                .setHourlyConsumptionL(hourlyConsumption)
                .addAllUsageEvents(usageEvents)
                .setLeakDetection(leakDetection)
                .setQuality(waterQuality)
                .build();
    }

    private SignalStrength getRandomSignalStrength() {
        List<SignalStrength> values = Arrays.stream(SignalStrength.values())
                .filter(level -> level != SignalStrength.UNRECOGNIZED)
                .toList();
        return values.get(random.nextInt(values.size()));
    }
}