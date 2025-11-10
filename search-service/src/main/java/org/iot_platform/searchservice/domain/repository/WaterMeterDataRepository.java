package org.iot_platform.searchservice.domain.repository;

import org.iot_platform.protos.water_meter_data.WaterMeterData;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WaterMeterDataRepository extends ElasticsearchRepository<WaterMeterData, String> {
}
