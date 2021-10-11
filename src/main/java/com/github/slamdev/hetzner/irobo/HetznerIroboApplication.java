package com.github.slamdev.hetzner.irobo;

import com.github.slamdev.hetzner.irobo.integration.AppConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing;

@SpringBootApplication
@EnableJdbcAuditing
@EnableConfigurationProperties(AppConfig.class)
public class HetznerIroboApplication {

    public static void main(String[] args) {
        SpringApplication.run(HetznerIroboApplication.class, args);
    }
}
