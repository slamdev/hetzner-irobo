package com.github.slamdev.hetzner.irobo.integration;

import org.springframework.test.context.TestPropertySource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@TestPropertySource(properties = {
        "hetzner-irobo.scheduling.enabled=false",
})
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DisableScheduler {
}
