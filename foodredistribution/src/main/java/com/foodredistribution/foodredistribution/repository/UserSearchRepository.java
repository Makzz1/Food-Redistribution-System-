package com.foodredistribution.foodredistribution.repository;

import java.util.List;

import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import com.foodredistribution.foodredistribution.document.UserDocument;

@Repository
public interface UserSearchRepository extends ElasticsearchRepository<UserDocument, String> {

    // Automatically converted to an Elasticsearch BKD Tree geo-distance query combined with a term query on 'role'.
    List<UserDocument> findByRoleAndLocationNear(String role, GeoPoint location, String distance);
}
