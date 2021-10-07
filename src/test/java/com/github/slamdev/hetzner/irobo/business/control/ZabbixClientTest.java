package com.github.slamdev.hetzner.irobo.business.control;

import com.github.slamdev.hetzner.irobo.business.entity.ZabbixHostDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@Disabled
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class ZabbixClientTest {

    @Autowired
    ZabbixClient client;

    @Test
    void getHosts() {
        ZabbixHostDto[] hosts = client.getHosts();
        log.info("{}", (Object) hosts);
    }
}
