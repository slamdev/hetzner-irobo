package com.github.slamdev.hetzner.irobo.integration;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.TestPropertySource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.sql.init.mode=never",
})
@AutoConfigureTestDatabase
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AutoconfigureNoopDb {
}
