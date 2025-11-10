package org.iot_platform.searchservice.domain.repository;

import org.iot_platform.searchservice.domain.entity.sensors.airQuiality.AirQualityDataDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AirQualityDataRepository extends ElasticsearchRepository<AirQualityDataDocument, String> {

    // ===== ПРОСТЫЕ ЗАПРОСЫ (без nested) =====

    /**
     * Поиск по комнате
     */
    List<AirQualityDataDocument> findByRoomNumber(String roomNumber);

    /**
     * Поиск по комнате с пагинацией
     */
    Page<AirQualityDataDocument> findByRoomNumber(String roomNumber, Pageable pageable);

    /**
     * Высокий уровень CO2
     */
    List<AirQualityDataDocument> findByCo2PpmGreaterThan(Integer threshold);

    /**
     * Поиск по диапазону CO2
     */
    List<AirQualityDataDocument> findByCo2PpmBetween(Integer min, Integer max);

    /**
     * Требуется вентиляция
     */
    List<AirQualityDataDocument> findByVentilationNeededTrue();

    /**
     * Текущий уровень качества воздуха
     */
    List<AirQualityDataDocument> findByCurrentAirQualityLevel(String level);

    /**
     * Плохое качество воздуха (POOR или хуже)
     */
    @Query("""
        {
          "terms": {
            "currentAirQualityLevel": ["POOR_AQ", "SEVERE_AQ", "HAZARDOUS_AQ"]
          }
        }
        """)
    List<AirQualityDataDocument> findPoorAirQuality();

    /**
     * Поиск за период
     */
    List<AirQualityDataDocument> findByTimestampBetween(Instant start, Instant end);

    /**
     * Комната + период
     */
    Page<AirQualityDataDocument> findByRoomNumberAndTimestampBetween(
            String roomNumber,
            Instant start,
            Instant end,
            Pageable pageable
    );

    /**
     * Подсчет по комнате
     */
    long countByRoomNumber(String roomNumber);

    /**
     * Последние данные от устройства
     */
    AirQualityDataDocument findFirstByDeviceIdOrderByTimestampDesc(String deviceId);

    // ===== NESTED ЗАПРОСЫ (для массива alerts) =====

    /**
     * Поиск документов с алертами определенной severity
     *
     * Пример: findByAlertSeverity("CRITICAL")
     */
    @Query("""
        {
          "nested": {
            "path": "alerts",
            "query": {
              "term": {
                "alerts.severity": "?0"
              }
            }
          }
        }
        """)
    List<AirQualityDataDocument> findByAlertSeverity(String severity);

    /**
     * Поиск документов с алертами по конкретному параметру
     *
     * Пример: findByAlertParameter("co2")
     */
    @Query("""
        {
          "nested": {
            "path": "alerts",
            "query": {
              "term": {
                "alerts.parameter": "?0"
              }
            }
          }
        }
        """)
    List<AirQualityDataDocument> findByAlertParameter(String parameter);

    /**
     * Поиск по severity И parameter
     *
     * Пример: findByAlertSeverityAndParameter("CRITICAL", "co2")
     */
    @Query("""
        {
          "nested": {
            "path": "alerts",
            "query": {
              "bool": {
                "must": [
                  { "term": { "alerts.severity": "?0" } },
                  { "term": { "alerts.parameter": "?1" } }
                ]
              }
            }
          }
        }
        """)
    List<AirQualityDataDocument> findByAlertSeverityAndParameter(
            String severity,
            String parameter
    );

    /**
     * Критичные алерты
     */
    @Query("""
        {
          "nested": {
            "path": "alerts",
            "query": {
              "term": {
                "alerts.severity": "CRITICAL"
              }
            }
          }
        }
        """)
    List<AirQualityDataDocument> findCriticalAlerts();

    /**
     * Алерты с превышением порога
     *
     * Пример: findAlertsExceedingThreshold(1000.0f)
     */
    @Query("""
        {
          "nested": {
            "path": "alerts",
            "query": {
              "range": {
                "alerts.currentValue": {
                  "gte": "?0"
                }
              }
            }
          }
        }
        """)
    List<AirQualityDataDocument> findAlertsExceedingThreshold(Float minValue);

    /**
     * Поиск по тексту recommendation внутри alerts
     *
     * Пример: findByAlertRecommendation("ventilation")
     */
    @Query("""
        {
          "nested": {
            "path": "alerts",
            "query": {
              "match": {
                "alerts.recommendation": "?0"
              }
            }
          }
        }
        """)
    List<AirQualityDataDocument> findByAlertRecommendation(String searchText);

    /**
     * Комната + критичные алерты
     */
    @Query("""
        {
          "bool": {
            "must": [
              { "term": { "roomNumber": "?0" } },
              {
                "nested": {
                  "path": "alerts",
                  "query": {
                    "term": {
                      "alerts.severity": "CRITICAL"
                    }
                  }
                }
              }
            ]
          }
        }
        """)
    List<AirQualityDataDocument> findByRoomNumberWithCriticalAlerts(String roomNumber);

    /**
     * Комната + период + критичные алерты
     */
    @Query("""
        {
          "bool": {
            "must": [
              { "term": { "roomNumber": "?0" } },
              { "range": { "timestamp": { "gte": "?1", "lte": "?2" } } },
              {
                "nested": {
                  "path": "alerts",
                  "query": {
                    "term": {
                      "alerts.severity": "CRITICAL"
                    }
                  }
                }
              }
            ]
          }
        }
        """)
    List<AirQualityDataDocument> findByRoomNumberAndPeriodWithCriticalAlerts(
            String roomNumber,
            Instant startTime,
            Instant endTime
    );

    /**
     * Документы с множественными алертами (2+)
     */
    @Query("""
        {
          "script": {
            "script": {
              "source": "doc['alerts'].size() >= params.minCount",
              "params": {
                "minCount": 2
              }
            }
          }
        }
        """)
    List<AirQualityDataDocument> findDocumentsWithMultipleAlerts();

}
