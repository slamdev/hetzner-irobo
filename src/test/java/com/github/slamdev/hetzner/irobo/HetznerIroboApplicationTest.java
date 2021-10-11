package com.github.slamdev.hetzner.irobo;

import com.github.slamdev.hetzner.irobo.integration.AutoconfigureEmbeddedBinder;
import com.github.slamdev.hetzner.irobo.integration.AutoconfigureEmbeddedDb;
import com.github.slamdev.hetzner.irobo.integration.WithRequiredProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WithRequiredProperties
@AutoconfigureEmbeddedDb
@AutoconfigureEmbeddedBinder
class HetznerIroboApplicationTest {

    @Test
    void should_load_context() {
        // noop
    }
}
