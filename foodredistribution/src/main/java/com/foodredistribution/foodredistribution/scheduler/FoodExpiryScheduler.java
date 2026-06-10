package com.foodredistribution.foodredistribution.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.foodredistribution.foodredistribution.entity.FoodPost;
import com.foodredistribution.foodredistribution.enums.FoodStatus;
import com.foodredistribution.foodredistribution.repository.FoodPostRepository;

@Component
public class FoodExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(FoodExpiryScheduler.class);

    private final FoodPostRepository foodPostRepository;

    public FoodExpiryScheduler(FoodPostRepository foodPostRepository) {
        this.foodPostRepository = foodPostRepository;
    }

    @Scheduled(fixedRate = 300000)
    public void expireFoodPosts() {

        List<FoodPost> expiredFoods = foodPostRepository
                .findByStatusAndExpiryTimeBefore(FoodStatus.AVAILABLE, LocalDateTime.now());

        for (FoodPost food : expiredFoods) {
            food.setStatus(FoodStatus.EXPIRED);
        }

        foodPostRepository.saveAll(expiredFoods);

        if (!expiredFoods.isEmpty()) {
            log.info("Expired food posts updated: {}", expiredFoods.size());
        }
    }
}