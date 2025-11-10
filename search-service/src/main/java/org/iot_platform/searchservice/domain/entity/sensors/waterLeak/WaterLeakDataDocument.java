package org.iot_platform.searchservice.domain.entity.sensors.waterLeak;

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
@Document(indexName = "water-leak-data")
public class WaterLeakDataDocument extends BaseSensorDocument {

    @Field(type = FieldType.Boolean)
    private Boolean leakDetected;

    @Field(type = FieldType.Keyword)
    private String severity; // enum - String

    @Field(type = FieldType.Keyword)
    private String location;

    @Field(type = FieldType.Long)
    private Long durationSeconds;

    @Field(type = FieldType.Float)
    private Float detectionConfidence;

    @Field(type = FieldType.Nested)
    private List<String> affectedZones;
}
