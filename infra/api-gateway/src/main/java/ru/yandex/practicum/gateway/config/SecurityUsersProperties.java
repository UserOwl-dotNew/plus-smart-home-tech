package ru.yandex.practicum.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "app.security")
@Data
public class SecurityUsersProperties {
    private List<User> users = new ArrayList<>();

    @Data
    public static class User {
        private String username;
        private String password;
        private List<String> roles = new ArrayList<>();
    }
}
