-- KEYS[1]: session_ages ZSET key
-- KEYS[2]: refresh_tokens HASH key
-- KEYS[3]: active_tokens key

-- ARGV[1]: maxDevices
-- ARGV[2]: newDeviceId
-- ARGV[3]: newExpiresAtTimestamp
-- ARGV[4]: newSessionValue ("{jti}:{refreshToken}")
-- ARGV[5]: userId
-- ARGV[6]: accessTokenTtlInSeconds
-- ARGV[7]: deleteActiveKeyPrefix

-- 최대 기기 수에 도달했거나 초과했다면
if redis.call('ZCARD', KEYS[1]) >= tonumber(ARGV[1]) then
    -- 1. ZSET에서 가장 오래된 deviceId를 가져오고 제거
    local oldest_device_id = redis.call('ZPOPMIN', KEYS[1])[1]
    if oldest_device_id then
        -- 2. HASH에서 해당 deviceId의 리프레시 토큰 삭제
        redis.call('HDEL', KEYS[2], oldest_device_id)

        -- 3. ZSET에서 가져온 deviceId로 바로 삭제할 활성 토큰 키를 만듦
        local active_key_to_delete = ARGV[7] .. ":" .. oldest_device_id
        redis.call('DEL', active_key_to_delete)
    end
end

-- 새 세션 정보 추가
redis.call('ZADD', KEYS[1], ARGV[3], ARGV[2])
redis.call('HSET', KEYS[2], ARGV[2], ARGV[4])
redis.call('SET', KEYS[3], ARGV[5], 'EX', ARGV[6])

return 1
