package com.reservation.customer.reservation.service;

import java.time.Duration;
import java.util.Optional;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reservation.customer.reservation.service.dto.TemporaryReservation;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class RedisReservationStore {
	private final RedisTemplate<String, String> redisTemplate;
	private final ObjectMapper objectMapper;

	public void save(TemporaryReservation reservation, Duration ttl) {
		try {
			String key = reservation.redisKey();
			String value = objectMapper.writeValueAsString(reservation);
			redisTemplate.opsForValue().set(key, value, ttl);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Redis 저장 중 JSON 직렬화 실패", e);
		}
	}

	public Optional<TemporaryReservation> find(String key) {
		String json = redisTemplate.opsForValue().get(key);
		if (json == null)
			return Optional.empty();

		try {
			return Optional.of(objectMapper.readValue(json, TemporaryReservation.class));
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Redis 조회 중 JSON 역직렬화 실패", e);
		}
	}
}
