package com.github.slamdev.hetzner.irobo.business.control;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@Disabled
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class HetznerRobotClientTest {

    @Autowired
    HetznerRobotClient client;

    @Test
    void getServers() {
        client.getServers();
    }
}
