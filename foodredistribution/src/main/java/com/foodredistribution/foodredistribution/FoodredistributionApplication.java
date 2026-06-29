package com.foodredistribution.foodredistribution;

import java.util.concurrent.Executor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@EnableAsync
@EnableScheduling
@SpringBootApplication
public class FoodredistributionApplication {

	public static void main(String[] args) {
		SpringApplication.run(FoodredistributionApplication.class, args);
		
	}

	/**
	 * Bounded thread pool for @Async tasks (email notifications, etc.).
	 * Prevents unbounded task creation when notifying many receivers.
	 */
	@Bean(name = "taskExecutor")
	public Executor taskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(5);
		executor.setMaxPoolSize(10);
		executor.setQueueCapacity(100);
		executor.setThreadNamePrefix("async-");
		executor.initialize();
		return executor;
	}

}
