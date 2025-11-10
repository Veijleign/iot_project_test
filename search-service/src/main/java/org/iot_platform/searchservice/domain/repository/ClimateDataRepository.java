package org.iot_platform.searchservice.domain.repository;

import org.iot_platform.protos.climate_data.ClimateData;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClimateDataRepository extends ElasticsearchRepository<ClimateData, String> {
}
