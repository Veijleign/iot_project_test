package org.iot_platform.searchservice.domain.entity.sensors.energyMeter;

import lombok.*;
import org.iot_platform.searchservice.domain.entity.sensors.BaseSensorDocument;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.util.Map;

@Setting(shards = 1, replicas = 0)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(indexName = "energy-meter-data")
public class EnergyMeterDataDocument extends BaseSensorDocument {

    @Field(type = FieldType.Double)
    private Double totalEnergyKwh;

    @Field(type = FieldType.Double)
    private Double currenPowerH;

    @Field(type = FieldType.Double)
    private Double hourlyConsumptionKwh;

    @Field(type = FieldType.Object)
    private Map<String, Double> circuitConsumption;

    // Power Quality
    @Field(type = FieldType.Float)
    private Float voltage;

    @Field(type = FieldType.Float)
    private Float frequency;

    @Field(type = FieldType.Float)
    private Float powerFactor;

    // Energy Alert
    @Field(type = FieldType.Keyword) // enum - String
    private String type;

    @Field(type = FieldType.Keyword)
    private String message;

    @Field(type = FieldType.Double)
    private Double threshold;

    @Field(type = FieldType.Double)
    private Double currentValue;
}
