package com.github.slamdev.hetzner.irobo;

import com.github.slamdev.hetzner.irobo.integration.AppConfig;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJdbcAuditing
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "1m")
@EnableConfigurationProperties(AppConfig.class)
public class HetznerIroboApplication {

    public static void main(String[] args) {
        SpringApplication.run(HetznerIroboApplication.class, args);
    }
}
