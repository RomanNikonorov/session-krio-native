package com.example.sessioncodec;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.ArrayList;
import java.util.List;

public final class SessionUserKryoSerializer extends Serializer<SessionUser> {

    @Override
    public void write(Kryo kryo, Output output, SessionUser user) {
        output.writeString(user.id());
        output.writeString(user.username());
        output.writeVarInt(user.roles().size(), true);
        for (String role : user.roles()) {
            output.writeString(role);
        }
    }

    @Override
    public SessionUser read(Kryo kryo, Input input, Class<? extends SessionUser> type) {
        String id = input.readString();
        String username = input.readString();
        int roleCount = input.readVarInt(true);
        List<String> roles = new ArrayList<>(roleCount);
        for (int i = 0; i < roleCount; i++) {
            roles.add(input.readString());
        }
        return new SessionUser(id, username, roles);
    }
}
