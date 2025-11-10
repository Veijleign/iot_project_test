package org.iot_platform.searchservice.domain.entity.sensors.climateData;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.iot_platform.searchservice.domain.entity.sensors.BaseSensorDocument;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

@Setting(shards = 1, replicas = 0)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(indexName = "iot-climate-data")
public class ClimateDataDocument extends BaseSensorDocument {
    // main fields
    @Field(type = FieldType.Float)
    private Float temperatureC;

    @Field(type = FieldType.Float)
    private Float humidityPercent;

    @Field(type = FieldType.Float)
    private Float heatIndex;

    @Field(type = FieldType.Float)
    private Float dewPoint;

    @Field(type = FieldType.Keyword)
    private String comfortLevel;

    @Field(type = FieldType.Boolean)
    private Boolean heatingRequired;

    @Field(type = FieldType.Boolean)
    private Boolean coolingRequired;

    @Field(type = FieldType.Boolean)
    private Boolean humidificationRequired;

    @Field(type = FieldType.Boolean)
    private Boolean dehumidificationRequired;
}