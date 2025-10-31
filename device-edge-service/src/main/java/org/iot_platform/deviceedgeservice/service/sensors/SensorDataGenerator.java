package org.iot_platform.deviceedgeservice.service.sensors;

public interface SensorDataGenerator<T> {

    /**
     * Data generator
     * */
    T generateData();

}
