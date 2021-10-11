package com.github.slamdev.hetzner.irobo.integration;

import com.google.common.net.InetAddresses;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
@DisableScheduler
@WithRequiredProperties
@AutoconfigureNoopDb
@AutoconfigureNoopBinder
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ZabbixApiTest {

    @Autowired
    ZabbixApi api;

    @Test
    @SuppressWarnings("UnstableApiUsage")
    void should_fetch_host_by_server_number() {
        Optional<ZabbixApi.Host> host = api.getHost(970480);
        assertThat(host).isPresent();
        assertThat(host.get().getHostId()).isEqualTo(10464);
        assertThat(host.get().getHostName()).isEqualTo("fsn-1-dc4-s3-970480");
        assertThat(host.get().getIp()).isEqualTo(InetAddresses.forString("10.3.2.220"));
    }
}
