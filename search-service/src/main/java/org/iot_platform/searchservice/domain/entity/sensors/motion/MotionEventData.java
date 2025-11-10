package org.iot_platform.searchservice.domain.entity.sensors.motion;

import lombok.*;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MotionEventData {
    @Field(type = FieldType.Long)
    private Long eventTimestamp;

    @Field(type = FieldType.Keyword)
    private String motionType; // enum - String

    @Field(type = FieldType.Keyword)
    private String zone;
}
