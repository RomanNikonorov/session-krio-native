package com.example.sessioncodec;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.SerializationException;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KryoRedisSerializerTest {

    private final KryoRedisSerializer serializer = new KryoRedisSerializer(new KryoFactory());

    @Test
    void roundTripsAllowedSessionAttributeTypes() {
        assertRoundTrip(new SessionUser("u-1", "alice", List.of("USER", "ADMIN")));
        assertRoundTrip("jvm-app");
        assertRoundTrip(42);
        assertRoundTrip(42L);
        assertRoundTrip(Instant.parse("2026-05-07T00:00:00Z"));
        assertRoundTrip(Duration.ofMinutes(30));
    }

    @Test
    void rejectsUnknownTypes() {
        assertThatThrownBy(() -> serializer.serialize(new UnknownSessionAttribute("boom")))
                .isInstanceOf(SerializationException.class)
                .hasMessageContaining("Failed to serialize");
    }

    @Test
    void producesStableBytesAcrossSerializerInstances() {
        SessionUser user = new SessionUser("u-1", "alice", List.of("USER"));

        byte[] first = serializer.serialize(user);
        byte[] second = new KryoRedisSerializer(new KryoFactory()).serialize(user);

        assertThat(second).isEqualTo(first);
        assertThat(HexFormat.of().formatHex(first)).startsWith("0101");
    }

    @Test
    void roundTripsNullWithVersionedEnvelope() {
        byte[] bytes = serializer.serialize(null);

        assertThat(bytes).containsExactly((byte) 1, (byte) 0);
        assertThat(serializer.deserialize(bytes)).isNull();
    }

    @Test
    void roundTripsSimpleMapWhenExplicitlyRegistered() {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("user", new SessionUser("u-1", "alice", List.of("USER")));
        value.put("counter", 1);

        assertRoundTrip(value);
    }

    private void assertRoundTrip(Object value) {
        assertThat(serializer.deserialize(serializer.serialize(value))).isEqualTo(value);
    }

    record UnknownSessionAttribute(String value) {
    }
}
