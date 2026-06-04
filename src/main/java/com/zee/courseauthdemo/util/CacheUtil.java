package com.zee.courseauthdemo.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @dev : Ezekiel Eromosei
 * @date : 04 Jun, 2026
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheUtil {

    public static final String EVENT="CACHE_UTILS";

    private final JsonUtil jsonUtil;
    private final RedisTemplate<String, String> redisTemplate;


    public <T> void setGenericData(String key, T data, boolean prettify,  long period, TimeUnit timeUnit){
        log.info("Event={}, message=saving data to cache with key={}", EVENT, key);
        try{
            redisTemplate.opsForValue().set(key, jsonUtil.toJSON(data, prettify), period, timeUnit);
        }catch (Exception e){
            log.error(e.getMessage(),e);
        }
    }

    public <T> void updateGenericData(String key, T data,  boolean prettify, long period, TimeUnit timeUnit){

        T availableData = getData(key, (Class<T>) data.getClass());
        try {
            if(availableData != null){
                log.info("Event={}, message=updating redis data with key: {}", EVENT, key);
                redisTemplate.opsForValue().set(key, jsonUtil.toJSON(data, prettify), period, timeUnit);
            }
        }catch (Exception e){
            log.error(e.getMessage(),e);
        }
    }

    public <T> T getData(String key, Class<T> clazz){
        String json = this.getString(key);
        if(json == null) return null;
        return jsonUtil.fromJSON(json, clazz);
    }


    public <T> List<T> getDataList(String key, Class<T> clazz){
        String json = this.getString(key);
        if(json == null) return Collections.emptyList();
        return jsonUtil.jsonToList(json, clazz);
    }

    public String getString(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        }catch (Exception ex){
            log.error(ex.getMessage(), ex);
            return null;
        }
    }

    public boolean removeKey(String key){
        if(StringUtils.isBlank(key)) return false;
        try {
            return Boolean.TRUE.equals(redisTemplate.delete(key));
        }catch (Exception ex){
            log.error(ex.getMessage(), ex);
            return false;
        }
    }
}
