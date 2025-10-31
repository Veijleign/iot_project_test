package org.iot_platform.deviceedgeservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class DeviceEdgeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeviceEdgeServiceApplication.class, args);
    }

//    @Override
//    public void run(String... args) throws Exception {
//        log.info("Device Edge Service started. Generating sensor data...");
//
//        // Держим приложение активным
//        Thread.currentThread().join();
//    }

}
