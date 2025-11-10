package org.iot_platform.searchservice.domain.entity.sensors.waterMeter;

import lombok.*;
import org.iot_platform.searchservice.domain.entity.sensors.BaseSensorDocument;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.util.List;

@Setting(shards = 1, replicas = 0)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(indexName = "water-meter-data")
public class WaterMeterDataDocument extends BaseSensorDocument {

    @Field(type = FieldType.Double)
    private Double totalConsumption;

    @Field(type = FieldType.Double)
    private Double flowRateLpm;

    @Field(type = FieldType.Double)
    private Double hourlyConsumptionL;

    @Field(type = FieldType.Nested)
    private List<WaterUsageEventData> usageEvents;

    // LeakDetection
    @Field(type = FieldType.Boolean)
    private Boolean possibleLeak;

    @Field(type = FieldType.Double)
    private Double leakRateLph;

    @Field(type = FieldType.Keyword)
    private String suspectedLocation;

    // WaterQuality
    @Field(type = FieldType.Float)
    private Double temperatureC;

    @Field(type = FieldType.Float)
    private Double phLevel;

    @Field(type = FieldType.Float)
    private Float turbidity;
}
