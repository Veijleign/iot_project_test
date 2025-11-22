package org.iot_platform.deviceedgeservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class DeviceEdgeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeviceEdgeServiceApplication.class, args);
    }

}
