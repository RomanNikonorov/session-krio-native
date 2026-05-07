package com.example.sessioncodec;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.redis.serializer.RedisSerializer;

import static org.assertj.core.api.Assertions.assertThat;

class KryoSessionSerializationConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(KryoSessionSerializationConfig.class));

    @Test
    void exposesSpringSessionDefaultRedisSerializerBeanName() {
        contextRunner.run(context -> {
            assertThat(context).hasBean("springSessionDefaultRedisSerializer");
            assertThat(context.getBean("springSessionDefaultRedisSerializer"))
                    .isInstanceOf(RedisSerializer.class)
                    .isInstanceOf(KryoRedisSerializer.class);
        });
    }
}
