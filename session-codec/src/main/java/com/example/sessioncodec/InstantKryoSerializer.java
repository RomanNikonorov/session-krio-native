package com.example.sessioncodec;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.time.Instant;

public final class InstantKryoSerializer extends Serializer<Instant> {

    @Override
    public void write(Kryo kryo, Output output, Instant instant) {
        output.writeLong(instant.getEpochSecond(), true);
        output.writeInt(instant.getNano(), true);
    }

    @Override
    public Instant read(Kryo kryo, Input input, Class<? extends Instant> type) {
        return Instant.ofEpochSecond(input.readLong(true), input.readInt(true));
    }
}
