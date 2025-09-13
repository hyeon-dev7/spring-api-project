package com.example.bookstore.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@EnableCaching
@Configuration
public class CacheConfig {
    // 캐시 생성
    private List<CaffeineCache> buildCaches() {
        List<CaffeineCache> caches = new ArrayList<>();
        for (CacheType cacheType : CacheType.values()){
            Caffeine<Object, Object> builder = Caffeine.newBuilder()
                    .maximumSize(cacheType.getMaximumSize());
            if (cacheType.getExpireAfterAccess()!=null){
                builder.expireAfterAccess(cacheType.getExpireAfterAccess(), TimeUnit.HOURS);
            }

            caches.add(new CaffeineCache(cacheType.getCacheName(), builder.build()));
        }
        return caches;
    }

    // 캐시 등록
    @Bean
    public CacheManager cacheManager(){
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(buildCaches());
        return cacheManager;
    }

}
