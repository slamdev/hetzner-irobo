package com.github.slamdev.hetzner.irobo.business.control;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@Disabled
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class ServerTagsUpdaterTest {

    @Autowired
    ServerTagsUpdater serverTagsUpdater;

    @Test
    void should_() {
        serverTagsUpdater.updateServerTags();
    }
}
