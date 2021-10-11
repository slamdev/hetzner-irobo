package com.github.slamdev.hetzner.irobo.integration;

import lombok.Builder;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Duration;

@Validated
@Value
@Builder
@ConstructorBinding
@ConfigurationProperties(prefix = "hetzner-irobo")
public class AppConfig {

    @Valid
    Robot robot;

    @Valid
    Zabbix zabbix;

    @Valid
    UpdateInterval updateInterval;

    @Value
    @Builder
    public static class Robot {
        @NotBlank
        String webUrl;

        @NotBlank
        String apiUrl;

        @NotBlank
        String username;

        @NotBlank
        String password;

        @NotBlank
        String sshKey;
    }

    @Value
    @Builder
    public static class Zabbix {
        @NotBlank
        String url;

        @NotBlank
        String username;

        @NotBlank
        String password;
    }

    @Value
    @Builder
    public static class UpdateInterval {
        @NotNull
        Duration hetznerServersList;
    }
}
