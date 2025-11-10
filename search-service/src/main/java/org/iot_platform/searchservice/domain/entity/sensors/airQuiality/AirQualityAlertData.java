package org.iot_platform.searchservice.domain.entity.sensors.airQuiality;

import lombok.*;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AirQualityAlertData {
    @Field(type = FieldType.Keyword)
    private String severity; // enum - String

    @Field(type = FieldType.Keyword)
    private String parameter;

    @Field(type = FieldType.Float)
    private Float currentValue;

    @Field(type = FieldType.Float)
    private Float threshold;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String recommendation;
}
