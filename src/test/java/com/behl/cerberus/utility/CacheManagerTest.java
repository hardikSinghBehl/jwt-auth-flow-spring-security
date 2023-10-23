package com.behl.cerberus.utility;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.fasterxml.jackson.databind.ObjectMapper;

@SuppressWarnings("unchecked")
class CacheManagerTest {

	private RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);
	private ObjectMapper objectMapper = mock(ObjectMapper.class);
	private CacheManager cacheManager = new CacheManager(redisTemplate, objectMapper);

	private ValueOperations<String, Object> valueOperations;

	@BeforeEach
	void setUp() {
		valueOperations = mock(ValueOperations.class);
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
	}

	@Test
	void shouldSaveValidKeyValuePairWithTtl() {
		final var key = "test-key";
		final var value = "test-value";
		final var timeToLive = mock(Duration.class);
		doNothing().when(valueOperations).set(key, value, timeToLive);

		cacheManager.save(key, value, timeToLive);

		verify(redisTemplate).opsForValue();
		verify(valueOperations).set(key, value, timeToLive);
	}

	@Test
	void shouldSaveKeyWithTtl() {
		final var key = "test-key";
		final var defaultValue = StringUtils.EMPTY;
		final var timeToLive = mock(Duration.class);
		doNothing().when(valueOperations).set(key, defaultValue, timeToLive);

		cacheManager.save(key, timeToLive);

		verify(redisTemplate).opsForValue();
		verify(valueOperations).set(key, defaultValue, timeToLive);
	}

	@Test
	public void shouldReturnTrueIfKeyPresentInCache() {
		final var key = "test-key";
		final var value = "test-value";
		when(valueOperations.get(key)).thenReturn(value);

		final var response = cacheManager.isPresent(key);

		assertThat(response).isTrue();
		verify(redisTemplate).opsForValue();
		verify(valueOperations).get(key);
	}

	@Test
	public void shouldReturnFalseIfKeyNotPresentInCache() {
		final var key = "test-key";
		when(valueOperations.get(key)).thenReturn(null);

		final var response = cacheManager.isPresent(key);

		assertThat(response).isFalse();
		verify(redisTemplate).opsForValue();
		verify(valueOperations).get(key);
	}

	@Test
	void shouldFetchValueForCachedKey() {
		final var key = "test-key";
		final var value = "test-value";
		when(valueOperations.get(key)).thenReturn(value);
		when(objectMapper.convertValue(value, String.class)).thenReturn(value);

		final var response = cacheManager.fetch(key, String.class);

		assertThat(response).isPresent().hasValue(value);
		verify(redisTemplate).opsForValue();
		verify(valueOperations).get(key);
		verify(objectMapper).convertValue(value, String.class);
	}

	@Test
	void shouldReturnEmptyOptionalIfKeyNotPresentInCache() {
		final var key = "test-key";
		when(valueOperations.get(key)).thenReturn(null);

		final var response = cacheManager.fetch(key, String.class);

		assertThat(response).isEmpty();
		verify(redisTemplate).opsForValue();
		verify(valueOperations).get(key);
		verify(objectMapper, times(0)).convertValue(any(), eq(String.class));
	}

	@Test
	void shouldThrowIllegalArgumentExceptionForNullArguments() {
		final var key = "test-key";
		final var value = "test-value";
		final var timeToLive = mock(Duration.class);

		assertThrows(IllegalArgumentException.class, () -> cacheManager.save(null, value, timeToLive));
		assertThrows(IllegalArgumentException.class, () -> cacheManager.save(key, null, timeToLive));
		assertThrows(IllegalArgumentException.class, () -> cacheManager.save(key, value, null));

		assertThrows(IllegalArgumentException.class, () -> cacheManager.save(null, timeToLive));
		assertThrows(IllegalArgumentException.class, () -> cacheManager.save(key, null));

		assertThrows(IllegalArgumentException.class, () -> cacheManager.isPresent(null));

		assertThrows(IllegalArgumentException.class, () -> cacheManager.fetch(key, null));
		assertThrows(IllegalArgumentException.class, () -> cacheManager.fetch(null, Class.class));
	}

}