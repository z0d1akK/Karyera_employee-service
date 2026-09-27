package com.zodiakk.employeeservice.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;

import java.time.Duration;
import java.util.Map;

import static com.zodiakk.employeeservice.common.cache.CacheNames.*;

@Configuration
@EnableCaching
public class RedisConfig {

    private static final String CACHE_PREFIX = "employee-service:";

    private static final Duration DEFAULT_TTL = Duration.ofMinutes(20);

    private static final Duration DICTIONARY_TTL = Duration.ofMinutes(60);

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        PolymorphicTypeValidator typeValidator =
                BasicPolymorphicTypeValidator.builder()
                        .allowIfBaseType(Object.class)
                        .build();

        GenericJacksonJsonRedisSerializer serializer =
                GenericJacksonJsonRedisSerializer.builder()
                        .enableDefaultTyping(typeValidator)
                        .build();

        RedisSerializationContext.SerializationPair<Object> serializationPair =
                RedisSerializationContext.SerializationPair
                        .fromSerializer(serializer);

        RedisCacheConfiguration defaultConfiguration =
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(DEFAULT_TTL)
                        .disableCachingNullValues()
                        .serializeValuesWith(serializationPair)
                        .prefixCacheNameWith(CACHE_PREFIX);

        Map<String, RedisCacheConfiguration> cacheConfigurations = Map.of(
                POSITIONS, createDictionaryConfiguration(serializationPair),
                GRADES, createDictionaryConfiguration(serializationPair),
                DEPARTMENTS, createDictionaryConfiguration(serializationPair),
                SPECIALIZATIONS, createDictionaryConfiguration(serializationPair),
                SKILLS, createDictionaryConfiguration(serializationPair),
                RESPONSIBILITY_TYPES, createDictionaryConfiguration(serializationPair)
        );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfiguration)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    private RedisCacheConfiguration createDictionaryConfiguration(RedisSerializationContext.SerializationPair<Object> serializationPair) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(DICTIONARY_TTL)
                .disableCachingNullValues()
                .serializeValuesWith(serializationPair)
                .prefixCacheNameWith(CACHE_PREFIX);
    }
}