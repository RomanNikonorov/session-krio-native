package com.example.sessioncodec;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.DefaultSerializers;
import com.esotericsoftware.kryo.serializers.CollectionSerializer;
import com.esotericsoftware.kryo.serializers.MapSerializer;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.UUID;

public final class SessionKryoRegistry {

    private SessionKryoRegistry() {
    }

    public static void register(Kryo kryo) {
        kryo.register(SessionUser.class, new SessionUserKryoSerializer(), 20);
        kryo.register(ArrayList.class, collectionSerializer(), 21);
        kryo.register(HashMap.class, mapSerializer(), 25);
        kryo.register(LinkedHashMap.class, mapSerializer(), 26);
        kryo.register(HashSet.class, collectionSerializer(), 27);
        kryo.register(LinkedHashSet.class, collectionSerializer(), 28);
        kryo.register(UUID.class, new DefaultSerializers.UUIDSerializer(), 31);
        kryo.register(Instant.class, new InstantKryoSerializer(), 32);
        kryo.register(Duration.class, new DurationKryoSerializer(), 33);
    }

    private static CollectionSerializer<?> collectionSerializer() {
        CollectionSerializer<?> serializer = new CollectionSerializer<>();
        serializer.setElementsCanBeNull(false);
        return serializer;
    }

    private static MapSerializer<?> mapSerializer() {
        MapSerializer<?> serializer = new MapSerializer<>();
        serializer.setKeysCanBeNull(false);
        serializer.setValuesCanBeNull(true);
        return serializer;
    }
}
