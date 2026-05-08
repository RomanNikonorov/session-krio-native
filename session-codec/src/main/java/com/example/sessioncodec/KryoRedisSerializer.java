package com.example.sessioncodec;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class KryoRedisSerializer implements RedisSerializer<Object> {

    private static final byte FORMAT_VERSION = 2;

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
            byte[] kryoPayload = writeKryoPayload(value);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream(kryoPayload.length);
            buffer.write(FORMAT_VERSION);
            buffer.write(1);
            buffer.write(compress(kryoPayload));
            return buffer.toByteArray();
        }
        catch (IOException | RuntimeException ex) {
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
        try {
            byte[] kryoPayload = decompress(bytes, 2, bytes.length - 2);
            return readKryoPayload(kryoPayload);
        }
        catch (IOException | RuntimeException ex) {
            throw new SerializationException("Failed to deserialize Spring Session value with Kryo", ex);
        }
    }

    private byte[] writeKryoPayload(Object value) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(256);
        try (Output output = new Output(buffer)) {
            kryo.get().writeClassAndObject(output, value);
        }
        return buffer.toByteArray();
    }

    private Object readKryoPayload(byte[] bytes) {
        try (Input input = new Input(bytes)) {
            return kryo.get().readClassAndObject(input);
        }
    }

    private static byte[] compress(byte[] bytes) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(bytes.length);
        try (GZIPOutputStream gzip = new GZIPOutputStream(buffer)) {
            gzip.write(bytes);
        }
        return buffer.toByteArray();
    }

    private static byte[] decompress(byte[] bytes, int offset, int length) throws IOException {
        try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(bytes, offset, length))) {
            return gzip.readAllBytes();
        }
    }
}
