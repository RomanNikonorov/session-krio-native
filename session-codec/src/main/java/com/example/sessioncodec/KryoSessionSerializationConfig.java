package com.example.sessioncodec;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.serializer.RedisSerializer;

@AutoConfiguration
public class KryoSessionSerializationConfig {

    @Bean
    public KryoFactory kryoFactory() {
        return new KryoFactory();
    }

    @Bean("springSessionDefaultRedisSerializer")
    public RedisSerializer<Object> springSessionDefaultRedisSerializer(KryoFactory kryoFactory) {
        return new KryoRedisSerializer(kryoFactory);
    }
}
