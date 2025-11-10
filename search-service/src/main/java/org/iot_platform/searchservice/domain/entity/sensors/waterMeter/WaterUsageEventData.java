package org.iot_platform.searchservice.domain.entity.sensors.waterMeter;

import lombok.*;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class WaterUsageEventData {
    @Field(type = FieldType.Long)
    private Long startTime;

    @Field(type = FieldType.Long)
    private Long endTime;

    @Field(type = FieldType.Double)
    private Double volumeL;

    @Field(type = FieldType.Keyword, analyzer = "standard")
    private String type;
}
