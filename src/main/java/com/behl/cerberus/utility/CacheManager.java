package com.behl.cerberus.utility;

import java.time.Duration;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheManager {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    
    public void save(@NonNull final String key, @NonNull final Object value, @NonNull final Duration timeToLive) {
        redisTemplate.opsForValue().set(key, value, timeToLive);
        log.info("Cached value with key '{}' for {} seconds", key, timeToLive.toSeconds());
    }
    
    public void save(@NonNull final String key, @NonNull final Duration timeToLive) {
        redisTemplate.opsForValue().set(key, StringUtils.EMPTY, timeToLive);
        log.info("Cached non value key '{}' for {} seconds", key, timeToLive.toSeconds());
    }
    
    public Boolean isPresent(@NonNull final String key) {
    	final var fetchedValue = redisTemplate.opsForValue().get(key);
    	return Optional.ofNullable(fetchedValue).isPresent();
    }

    public <T> Optional<T> fetch(@NonNull final String key, @NonNull final Class<T> targetClass) {
        final var value = Optional.ofNullable(redisTemplate.opsForValue().get(key));
        if (value.isEmpty()) {
            log.info("No cached value found for key '{}'", key);
            return Optional.empty();          
        }
        T result = objectMapper.convertValue(value.get(), targetClass);
        log.info("Fetched cached value with key '{}'", key);
        return Optional.of(result);
    }

}