package com.foodredistribution.foodredistribution.interceptor;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.foodredistribution.foodredistribution.annotation.RateLimit;
import com.foodredistribution.foodredistribution.exception.RateLimitException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true; // Not a controller method, skip
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        
        // 1. Check for method-level annotation
        RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);
        
        // 2. Fallback to class-level annotation
        if (rateLimit == null) {
            rateLimit = handlerMethod.getBeanType().getAnnotation(RateLimit.class);
        }

        // If no rate limit annotation found, proceed normally
        if (rateLimit == null) {
            return true;
        }

        int maxRequests = rateLimit.requests();
        int timeWindow = rateLimit.window();
        String groupKey = rateLimit.key();

        // Determine the client IP
        String clientIp = getClientIp(request);
        
        // Determine the resource key
        String resourceKey = groupKey.isEmpty() ? request.getRequestURI() : groupKey;

        // Redis Key structure: rate_limit:{IP}:{Resource}
        String redisKey = "rate_limit:" + clientIp + ":" + resourceKey;

        // Increment count
        Long count = redisTemplate.opsForValue().increment(redisKey);

        // If it's the first request, set the expiration window
        if (count != null && count == 1) {
            redisTemplate.expire(redisKey, timeWindow, TimeUnit.SECONDS);
        }

        // Check if exceeded
        if (count != null && count > maxRequests) {
            throw new RateLimitException("Too many requests. Please try again later.");
        }

        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || "unknown".equalsIgnoreCase(xfHeader)) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim(); // Get the first IP in the list
    }
}
