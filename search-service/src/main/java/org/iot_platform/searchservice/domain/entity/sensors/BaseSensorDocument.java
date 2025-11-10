package org.iot_platform.searchservice.domain.entity.sensors;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;

@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class BaseSensorDocument {
    @Id
    private String id;

    // sensorHeader
    @Field(type = FieldType.Keyword)
    private String deviceId;

    @Field(type = FieldType.Date)
    private Instant timestamp;

    @Field(type = FieldType.Keyword)
    private String hotelId;

    @Field(type = FieldType.Keyword)
    private String roomNumber;

    @Field(type = FieldType.Float)
    private Float batteryLevel;

    @Field(type = FieldType.Keyword)
    private Float signalStrength; // enum - String
}
