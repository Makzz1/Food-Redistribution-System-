package com.foodredistribution.foodredistribution.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import com.foodredistribution.foodredistribution.document.FoodPostDocument;

@Repository
public interface FoodPostSearchRepository extends ElasticsearchRepository<FoodPostDocument, String> {

    // Spring Data Elasticsearch method to find points near a location within a specific distance (e.g., "5km")
    Page<FoodPostDocument> findByLocationNear(GeoPoint location, String distance, Pageable pageable);
}
