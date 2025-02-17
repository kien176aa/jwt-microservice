package com.javatechie.service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javatechie.entity.UrlAccess;
import com.javatechie.repository.UrlAccessRepository;
import lombok.extern.slf4j.Slf4j;
import org.example.constants.ErrorMessage;
import org.example.exception.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Slf4j
public class UrlAccessService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UrlAccessRepository urlAccessRepository;

    private static final String CACHE_KEY = "url_access_list"; // Key Redis

    public List<UrlAccess> getAllUrlAccess() {
        try {
            Object cachedData = redisTemplate.opsForValue().get(CACHE_KEY);
            if (cachedData != null) {
                log.info("Get in redis");
                return objectMapper.convertValue(cachedData, new TypeReference<List<UrlAccess>>() {});
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ParseException(ErrorMessage.PARSE_ERROR);
        }

        List<UrlAccess> urlAccessList = urlAccessRepository.findAll();
        log.info("Get in db");
        redisTemplate.opsForValue().set(CACHE_KEY, urlAccessList);

        return urlAccessList;
    }

    public void clearCache() {
        redisTemplate.delete(CACHE_KEY);
    }

    public void clearAllRedis() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }
}
