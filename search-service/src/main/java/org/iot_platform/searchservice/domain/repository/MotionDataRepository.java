package org.iot_platform.searchservice.domain.repository;

import org.iot_platform.searchservice.domain.entity.sensors.motion.MotionDataDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MotionDataRepository extends ElasticsearchRepository<MotionDataDocument, String> {
}
