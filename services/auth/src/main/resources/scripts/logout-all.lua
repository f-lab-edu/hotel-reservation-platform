-- KEYS[1]: refresh_tokens HASH key
-- KEYS[2]: session_ages ZSET key

-- ARGV[1]: TEMP UUID

-- 1. Hash에 저장된 모든 세션 정보를 가져옴 (Field: deviceId, Value: sessionInfoJson)
local all_sessions = redis.call('HGETALL', KEYS[1])

-- 2. 모든 Value(JSON)를 순회하며 jti를 추출하고, 해당 active_jti key를 삭제
--    i=2부터 2씩 증가하며 Value만 순회
for i = 2, #all_sessions, 2 do
    local session_info_json = all_sessions[i]

    -- 3. RedisJSON 명령어를 사용해 JSON에서 accessTokenJti 값을 추출합니다.
    --    JSON.GET은 Key에 대해서만 동작하므로, 임시 Key에 값을 잠깐 저장했다가 사용합니다.
    redis.call('JSON.SET', ARGV[1], '$', session_info_json)
    local jti_to_delete_array_string = redis.call('JSON.GET', ARGV[1], 'accessTokenJti')

    -- 4. 추출한 jti로 활성 액세스 토큰 키를 삭제합니다.
    if jti_to_delete_array_string and #jti_to_delete_array_string > 0 then
        -- JSON.GET은 결과를 JSON 배열 문자열 '["uuid-123"]' 형태로 반환하므로, 따옴표를 제거합니다.
        local jti_to_delete = string.sub(jti_to_delete_array_string, 2, -2)
        redis.call('DEL', "active_jti:" .. jti_to_delete)
    end

    -- 5. 사용한 임시 Key를 삭제합니다.
    redis.call('DEL', ARGV[1])
end

-- 6. Hash와 ZSET Key 자체를 삭제하여 모든 리프레시 토큰 무효화
redis.call('DEL', KEYS[1])
redis.call('DEL', KEYS[2])

return #all_sessions / 2 -- 삭제한 세션의 개수를 반환
