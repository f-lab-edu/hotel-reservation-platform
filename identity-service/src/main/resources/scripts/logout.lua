-- KEYS[1]: refresh_tokens HASH key
-- KEYS[2]: session_ages ZSET key
-- KEYS[3]: active_jti key

-- ARGV[1]: deviceId

-- 1. Hash에서 특정 기기(deviceId)의 리프레시 토큰 세션 삭제
redis.call('HDEL', KEYS[1], ARGV[1])

-- 2. ZSET에서 해당 deviceId 멤버 삭제
redis.call('ZREM', KEYS[2], ARGV[1])

-- 3. 현재 Access Token 관련 Active 정보를 삭제
redis.call('DEL', KEYS[3])

return 1
