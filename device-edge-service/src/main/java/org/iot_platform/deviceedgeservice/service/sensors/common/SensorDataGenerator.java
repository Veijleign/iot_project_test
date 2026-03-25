package org.iot_platform.deviceedgeservice.service.sensors.common;

public interface SensorDataGenerator<T> {

    /**
     * Data generator
     * */
    T generateData();

}
