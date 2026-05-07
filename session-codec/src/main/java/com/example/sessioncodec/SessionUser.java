package com.example.sessioncodec;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public record SessionUser(String id, String username, List<String> roles) implements Serializable {

    public SessionUser {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(username, "username must not be null");
        roles = List.copyOf(Objects.requireNonNull(roles, "roles must not be null"));
    }
}
