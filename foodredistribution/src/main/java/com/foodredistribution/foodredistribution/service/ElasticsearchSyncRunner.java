package com.foodredistribution.foodredistribution.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.foodredistribution.foodredistribution.document.FoodPostDocument;
import com.foodredistribution.foodredistribution.document.UserDocument;
import com.foodredistribution.foodredistribution.entity.FoodPost;
import com.foodredistribution.foodredistribution.entity.User;
import com.foodredistribution.foodredistribution.repository.FoodPostRepository;
import com.foodredistribution.foodredistribution.repository.FoodPostSearchRepository;
import com.foodredistribution.foodredistribution.repository.UserRepository;
import com.foodredistribution.foodredistribution.repository.UserSearchRepository;

@Component
public class ElasticsearchSyncRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchSyncRunner.class);

    private final UserRepository userRepository;
    private final UserSearchRepository userSearchRepository;
    private final FoodPostRepository foodPostRepository;
    private final FoodPostSearchRepository foodPostSearchRepository;
    private final org.springframework.data.elasticsearch.core.ElasticsearchOperations elasticsearchOperations;

    public ElasticsearchSyncRunner(
            UserRepository userRepository,
            UserSearchRepository userSearchRepository,
            FoodPostRepository foodPostRepository,
            FoodPostSearchRepository foodPostSearchRepository,
            org.springframework.data.elasticsearch.core.ElasticsearchOperations elasticsearchOperations
    ) {
        this.userRepository = userRepository;
        this.userSearchRepository = userSearchRepository;
        this.foodPostRepository = foodPostRepository;
        this.foodPostSearchRepository = foodPostSearchRepository;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Starting legacy data sync to Elasticsearch...");

        var userIndexOps = elasticsearchOperations.indexOps(UserDocument.class);
        if (!userIndexOps.exists()) {
            userIndexOps.create();
            userIndexOps.putMapping(userIndexOps.createMapping());
            logger.info("Created users index in Elasticsearch.");
        }

        var foodIndexOps = elasticsearchOperations.indexOps(FoodPostDocument.class);
        if (!foodIndexOps.exists()) {
            foodIndexOps.create();
            foodIndexOps.putMapping(foodIndexOps.createMapping());
            logger.info("Created food_posts index in Elasticsearch.");
        }

        syncUsers();
        syncFoodPosts();

        logger.info("Elasticsearch data sync completed.");
    }

    private void syncUsers() {
        List<User> users = userRepository.findAll();
        int count = 0;
        
        for (User user : users) {
            // Only sync users who have set their coordinates
            if (user.getLatitude() != null && user.getLongitude() != null) {
                UserDocument doc = new UserDocument(
                        String.valueOf(user.getId()),
                        user.getEmail(),
                        user.getRole().name(),
                        new GeoPoint(user.getLatitude(), user.getLongitude())
                );
                userSearchRepository.save(doc);
                count++;
            }
        }
        logger.info("Synced {} users to Elasticsearch.", count);
    }

    private void syncFoodPosts() {
        List<FoodPost> posts = foodPostRepository.findAll();
        int count = 0;
        
        for (FoodPost post : posts) {
            if (post.getLatitude() != null && post.getLongitude() != null) {
                FoodPostDocument doc = new FoodPostDocument(
                        String.valueOf(post.getId()),
                        post.getFoodName(),
                        post.getDescription(),
                        post.getStatus().name(),
                        new GeoPoint(post.getLatitude(), post.getLongitude())
                );
                foodPostSearchRepository.save(doc);
                count++;
            }
        }
        logger.info("Synced {} food posts to Elasticsearch.", count);
    }
}
