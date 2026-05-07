package com.example.nativeapp;

import com.example.sessioncodec.SessionUser;
import jakarta.servlet.http.HttpSession;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class NativeApplication {

    public static void main(String[] args) {
        SpringApplication.run(NativeApplication.class, args);
    }

    @RestController
    static class SessionController {

        @GetMapping("/session/create")
        Map<String, Object> create(HttpSession session) {
            SessionUser user = new SessionUser("u-200", "native-user", List.of("USER", "NATIVE_WRITER"));
            session.setAttribute("user", user);
            session.setAttribute("writer", "native-app");
            session.setAttribute("counter", 1);
            session.setAttribute("createdAt", Instant.parse("2026-05-07T00:00:00Z"));
            return describe(session);
        }

        @GetMapping("/session/increment")
        Map<String, Object> increment(HttpSession session) {
            Integer counter = (Integer) session.getAttribute("counter");
            session.setAttribute("counter", counter == null ? 1 : counter + 1);
            return describe(session);
        }

        @GetMapping("/session")
        Map<String, Object> read(HttpSession session) {
            return describe(session);
        }

        private Map<String, Object> describe(HttpSession session) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("app", "native-app");
            response.put("sessionId", session.getId());
            response.put("user", session.getAttribute("user"));
            response.put("writer", session.getAttribute("writer"));
            response.put("counter", session.getAttribute("counter"));
            response.put("createdAt", session.getAttribute("createdAt"));
            return response;
        }
    }
}
