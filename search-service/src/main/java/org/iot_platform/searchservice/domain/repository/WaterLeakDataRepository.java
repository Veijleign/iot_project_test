package org.iot_platform.searchservice.domain.repository;

import org.iot_platform.protos.water_leak_data.WaterLeakData;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WaterLeakDataRepository extends ElasticsearchRepository<WaterLeakData, String> {
}
