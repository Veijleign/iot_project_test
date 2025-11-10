package org.iot_platform.searchservice.domain.entity.sensors.motion;

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
@Document(indexName = "motion-data")
public class MotionDataDocument extends BaseSensorDocument {

    @Field(type = FieldType.Boolean)
    private Boolean motionDetected;

    @Field(type = FieldType.Integer)
    private Integer detectionCount;

    @Field(type = FieldType.Float)
    private Float detectionConfidence;

    @Field(type = FieldType.Nested) // nested
    private List<MotionEventData> events;

    @Field(type = FieldType.Keyword)
    private String roomStatus; // enum - String
}
