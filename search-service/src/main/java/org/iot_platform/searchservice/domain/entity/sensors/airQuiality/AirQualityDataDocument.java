package org.iot_platform.searchservice.domain.entity.sensors.airQuiality;

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
@Document(indexName = "air-quality-data")
public class AirQualityDataDocument extends BaseSensorDocument {
    // main fields
    @Field(type = FieldType.Integer)
    private Integer co2Ppm;

    @Field(type = FieldType.Float)
    private Float tvocPpb;

    @Field(type = FieldType.Float)
    private Float pm25;

    @Field(type = FieldType.Float)
    private Float pm10;

    @Field(type = FieldType.Float)
    private Float oxygenPercent;

    @Field(type = FieldType.Integer)
    private Integer airQualityIndex;

    // VentilationRecommendation
    @Field(type = FieldType.Boolean)
    private Boolean ventilationNeeded;

    @Field(type = FieldType.Integer)
    private Integer recommendedDurationMin;

    @Field(type = FieldType.Keyword)
    private String currentAirQualityLevel;  // enum - String

    @Field(type = FieldType.Keyword)
    private String targetAirQualityLevel;  // enum - String

    // Array of Alerts
    @Field(type = FieldType.Nested)
    private List<AirQualityAlertData> alerts;

}
