package com.github.slamdev.hetzner.irobo.integration;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.utility.DockerImageName;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ExtendWith(AutoconfigureEmbeddedBinder.NatsExtension.class)
public @interface AutoconfigureEmbeddedBinder {

    class NatsExtension implements BeforeAllCallback, AfterAllCallback {

        private NatsContainer natsContainer;

        @Override
        public void beforeAll(ExtensionContext context) {
            natsContainer = new NatsContainer();
            natsContainer.start();
            System.setProperty("nats.spring.server=", "nats://localhost:" + natsContainer.getMappedPort(4222));
        }

        @Override
        public void afterAll(ExtensionContext context) {
            natsContainer.stop();
        }
    }

    class NatsContainer extends GenericContainer<NatsContainer> {
        public static final DockerImageName DEFAULT_NATS_IMAGE = DockerImageName.parse("nats:2.6.1-alpine3.14");
        public static final int NATS_PORT = 4222;
        public static final int NATS_MGMT_PORT = 8222;

        public NatsContainer() {
            this(DEFAULT_NATS_IMAGE);
        }

        public NatsContainer(String dockerImageName) {
            this(DockerImageName.parse(dockerImageName));
        }

        public NatsContainer(DockerImageName dockerImageName) {
            super(dockerImageName);
            addExposedPort(NATS_PORT);
            addExposedPort(NATS_MGMT_PORT);
            waitStrategy = new LogMessageWaitStrategy().withRegEx(".*Server is ready.*");
        }
    }
}
