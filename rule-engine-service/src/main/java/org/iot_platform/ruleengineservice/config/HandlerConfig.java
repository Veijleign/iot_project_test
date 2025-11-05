package org.iot_platform.ruleengineservice.config;

import lombok.extern.slf4j.Slf4j;
import org.iot_platform.ruleengineservice.handler.common.SensorDataHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Configuration
public class HandlerConfig {

    @Bean
    public Map<String, SensorDataHandler<?>> sensorDataHandlers(List<SensorDataHandler<?>> handlerList) {
        return handlerList.stream()
                .collect(Collectors.toMap(
                        SensorDataHandler::getTopicName,
                        Function.identity()
                ));
    }
}
