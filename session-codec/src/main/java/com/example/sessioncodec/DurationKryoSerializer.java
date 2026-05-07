package com.example.sessioncodec;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.time.Duration;

public final class DurationKryoSerializer extends Serializer<Duration> {

    @Override
    public void write(Kryo kryo, Output output, Duration duration) {
        output.writeLong(duration.getSeconds(), true);
        output.writeInt(duration.getNano(), true);
    }

    @Override
    public Duration read(Kryo kryo, Input input, Class<? extends Duration> type) {
        return Duration.ofSeconds(input.readLong(true), input.readInt(true));
    }
}
