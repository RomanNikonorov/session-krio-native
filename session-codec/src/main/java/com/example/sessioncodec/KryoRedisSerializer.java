package com.example.sessioncodec;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.io.ByteArrayOutputStream;

public final class KryoRedisSerializer implements RedisSerializer<Object> {

    private static final byte FORMAT_VERSION = 1;

    private final ThreadLocal<Kryo> kryo;

    public KryoRedisSerializer(KryoFactory kryoFactory) {
        this.kryo = ThreadLocal.withInitial(kryoFactory::create);
    }

    @Override
    public byte[] serialize(Object value) throws SerializationException {
        if (value == null) {
            return new byte[] { FORMAT_VERSION, 0 };
        }
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream(256);
            buffer.write(FORMAT_VERSION);
            buffer.write(1);
            try (Output output = new Output(buffer)) {
                kryo.get().writeClassAndObject(output, value);
            }
            return buffer.toByteArray();
        }
        catch (RuntimeException ex) {
            throw new SerializationException("Failed to serialize Spring Session value with Kryo", ex);
        }
    }

    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        if (bytes.length < 2 || bytes[0] != FORMAT_VERSION) {
            throw new SerializationException("Unsupported Kryo session payload version");
        }
        if (bytes[1] == 0) {
            return null;
        }
        try (Input input = new Input(bytes, 2, bytes.length - 2)) {
            return kryo.get().readClassAndObject(input);
        }
        catch (RuntimeException ex) {
            throw new SerializationException("Failed to deserialize Spring Session value with Kryo", ex);
        }
    }
}
