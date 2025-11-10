package org.iot_platform.searchservice.domain.repository;

import org.iot_platform.protos.energy_meter_data.EnergyMeterData;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnergyMeterDataRepository extends ElasticsearchRepository<EnergyMeterData, String> {
}
