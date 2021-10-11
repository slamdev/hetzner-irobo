package com.github.slamdev.hetzner.irobo.business.boundary;

import com.github.slamdev.hetzner.irobo.business.entity.HetznerServerMessage;
import com.github.slamdev.hetzner.irobo.integration.AutoconfigureNoopBinder;
import com.github.slamdev.hetzner.irobo.integration.AutoconfigureNoopDb;
import com.github.slamdev.hetzner.irobo.integration.DisableScheduler;
import com.github.slamdev.hetzner.irobo.integration.WithRequiredProperties;
import com.google.common.net.InetAddresses;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@DisableScheduler
@WithRequiredProperties
@AutoconfigureNoopDb
@AutoconfigureNoopBinder
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class HetznerServerProducerTest {

    @Autowired
    private HetznerServerProducer hetznerServerProducer;

    @MockBean
    private HetznerServerConsumer hetznerServerConsumer;

    @Captor
    private ArgumentCaptor<HetznerServerMessage> serverCaptor;

    @Test
    @SuppressWarnings("UnstableApiUsage")
    void should_send_server_messages() {
        List<HetznerServerMessage> servers = List.of(
                HetznerServerMessage.builder()
                        .id(1)
                        .name("srv1")
                        .product("p1")
                        .dc("dc1")
                        .ipV4(InetAddresses.forString("127.0.0.1"))
                        .ipV6(InetAddresses.forString("2001:0db8:85a3:0000:0000:8a2e:0370:7334"))
                        .cancelled(true)
                        .build(),
                HetznerServerMessage.builder()
                        .id(2)
                        .name("srv2")
                        .product("p2")
                        .dc("dc2")
                        .ipV4(InetAddresses.forString("192.168.0.1"))
                        .ipV6(InetAddresses.forString("2001:db8:3333:4444:CCCC:DDDD:EEEE:FFFF"))
                        .cancelled(false)
                        .build()
        );

        servers.forEach(hetznerServerProducer::sendHetznerServer);

        verify(hetznerServerConsumer, timeout(TimeUnit.SECONDS.toMillis(5)).times(2))
                .accept(serverCaptor.capture());
        List<HetznerServerMessage> actual = serverCaptor.getAllValues();
        assertThat(actual).containsExactlyInAnyOrderElementsOf(servers);
    }
}
