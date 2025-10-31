package org.iot_platform.deviceedgeservice.service.sensors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iot_platform.protos.energy_meter_data.*;
import org.iot_platform.protos.sensor_common.SensorHeader;
import org.iot_platform.protos.sensor_common.SignalStrength;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class EnergyMeterDataGenerator implements SensorDataGenerator<EnergyMeterData> {

    private final Random random = new Random();
    private final Map<String, Double> totalEnergyMap = new HashMap<>();

    public EnergyMeterData generateData() {
        String roomNumber = String.valueOf(random.nextInt(100) + 1);
        String hotelId = "HOTEL_" + (random.nextInt(5) + 1);

        SensorHeader header = SensorHeader.newBuilder()
                .setDeviceId(UUID.randomUUID().toString())
                .setDeviceType("ENERGY_METER")
                .setTimestamp(System.currentTimeMillis())
                .setHotelId(hotelId)
                .setRoomNumber(roomNumber)
                .setBatteryLevel(85.0f + random.nextFloat() * 15.0f)
                .setSignalStrength(getRandomSignalStrength())
                .build();

        String energyKey = hotelId + "_" + roomNumber;
        double baseConsumption = totalEnergyMap.getOrDefault(energyKey, 0.0);
        double hourlyConsumption = 0.1 + random.nextDouble() * 2.0; // 0.1-2.1 kWh
        double totalEnergy = baseConsumption + hourlyConsumption;
        totalEnergyMap.put(energyKey, totalEnergy);

        // Circuit consumption
        Map<String, Double> circuits = new HashMap<>();
        circuits.put("lighting", 0.1 + random.nextDouble() * 0.5);
        circuits.put("hvac", 0.2 + random.nextDouble() * 1.1);
        circuits.put("outlets", 0.05 + random.nextDouble() * 0.3);
        circuits.put("appliances", 0.1 + random.nextDouble() * 0.4);

        PowerQuality powerQuality = PowerQuality.newBuilder()
                .setVoltage(220.0f + (random.nextFloat() * 10 - 5))
                .setFrequency(50.0f)
                .setPowerFactor(0.9f + random.nextFloat() * 0.1f)
                .build();

        // Alert logic
        EnergyAlert alert;
        if (hourlyConsumption > 1.5) {
            alert = EnergyAlert.newBuilder()
                    .setType(AlertType.HIGH_CONSUMPTION)
                    .setMessage("High energy consumption detected")
                    .setThreshold(1.5)
                    .setCurrentValue(hourlyConsumption)
                    .build();
        } else if (random.nextDouble() < 0.05) { // 5% chance of power quality issue
            alert = EnergyAlert.newBuilder()
                    .setType(AlertType.POWER_QUALITY_ISSUE)
                    .setMessage("Voltage fluctuation detected")
                    .setThreshold(225.0)
                    .setCurrentValue(215.0 + random.nextDouble() * 15)
                    .build();
        } else {
            alert = EnergyAlert.newBuilder()
                    .setType(AlertType.NO_ALERT)
                    .build();
        }

        return EnergyMeterData.newBuilder()
                .setHeader(header)
                .setTotalEnergyKwh(totalEnergy)
                .setCurrentPowerW(hourlyConsumption * 1000)
                .setHourlyConsumptionKwh(hourlyConsumption)
                .putAllCircuitConsumption(circuits)
                .setPowerQuality(powerQuality)
                .setAlert(alert)
                .build();
    }

    private SignalStrength getRandomSignalStrength() {
        List<SignalStrength> values = Arrays.stream(SignalStrength.values())
                .filter(level -> level != SignalStrength.UNRECOGNIZED)
                .toList();
        return values.get(random.nextInt(values.size()));
    }
}