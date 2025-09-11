-- KEYS[1]: session_ages ZSET key (예: "session_ages:123")
-- KEYS[2]: refresh_tokens HASH key (예: "refresh_tokens:123")
-- KEYS[3]: New active_jti key (예: "active_jti:new-uuid-123")
-- KEYS[4]: Past active_jti key (예: "active_jti:new-uuid-123")

-- ARGV[1]: maxDevices
-- ARGV[2]: newDeviceId
-- ARGV[3]: newExpiresAtTimestamp (ZSET Score)
-- ARGV[4]: newSessionInfoJson
-- ARGV[5]: userId (새 jti의 Value)
-- ARGV[6]: newAccessTokenTtlInSeconds
-- ARGV[7]: newRefreshTokenTtlInSeconds

local count = redis.call('ZCARD', KEYS[1])
local max = tonumber(ARGV[1])

if count >= max and KEYS[4] ~= nil then
    -- 1. ZSET에서 가장 오래된 deviceId를 가져오고 제거합니다.
    local oldest_device_id_array = redis.call('ZPOPMIN', KEYS[1])
    if oldest_device_id_array and #oldest_device_id_array > 0 then
        local oldest_device_id = oldest_device_id_array[1]

        -- 2. HASH에서 해당 deviceId의 세션 정보(JSON)를 가져옵니다.
        local oldest_session_info_json = redis.call('HGET', KEYS[2], oldest_device_id)
        if oldest_session_info_json then
            -- 3. RedisJSON 명령어를 사용해 JSON에서 accessTokenJti 값을 추출합니다.
            --    JSON.GET은 Key에 대해서만 동작하므로, 임시 Key에 값을 잠깐 저장했다가 사용합니다.
            redis.call('JSON.SET', 'temp_session_json_for_jti', '$', oldest_session_info_json)
            local jti_to_delete_array_string = redis.call('JSON.GET', 'temp_session_json_for_jti', 'accessTokenJti')

            -- 4. 추출한 jti로 활성 액세스 토큰 키를 삭제합니다.
            if jti_to_delete_array_string and #jti_to_delete_array_string > 0 then
                -- JSON.GET은 결과를 JSON 배열 문자열 '["uuid-123"]' 형태로 반환하므로, 따옴표를 제거합니다.
                local jti_to_delete = string.sub(jti_to_delete_array_string, 2, -2)
                redis.call('DEL', "active_jti:" .. jti_to_delete)
            end

            -- 5. 사용한 임시 Key를 삭제합니다.
            redis.call('DEL', 'temp_session_json_for_jti')
        end

        -- 6. HASH에서 오래된 리프레시 토큰 세션을 삭제합니다.
        redis.call('HDEL', KEYS[2], oldest_device_id)
    end
end

-- 새 세션 정보 추가
redis.call('ZADD', KEYS[1], ARGV[3], ARGV[2])
redis.call('HSET', KEYS[2], ARGV[2], ARGV[4])
redis.call('SET', KEYS[3], ARGV[5], 'EX', ARGV[6])

redis.call('EXPIRE', KEYS[1], ARGV[7])
redis.call('EXPIRE', KEYS[2], ARGV[7])

-- 과거 Active JTI 삭제
if KEYS[4] then
    redis.call('DEL', KEYS[4])
end

return 1
