package com.github.slamdev.hetzner.irobo.integration;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.CockroachContainer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ExtendWith(AutoconfigureEmbeddedDb.CockroachExtension.class)
public @interface AutoconfigureEmbeddedDb {

    class CockroachExtension implements BeforeAllCallback, AfterAllCallback {

        private CockroachContainer cockroachContainer;

        @Override
        public void beforeAll(ExtensionContext context) {
            cockroachContainer = new CockroachContainer("cockroachdb/cockroach:v21.1.9");
            cockroachContainer.start();
            System.setProperty("spring.datasource.url", cockroachContainer.getJdbcUrl());
            System.setProperty("spring.datasource.username", cockroachContainer.getUsername());
            System.setProperty("spring.datasource.password", cockroachContainer.getPassword());
        }

        @Override
        public void afterAll(ExtensionContext context) {
            cockroachContainer.stop();
        }
    }
}
