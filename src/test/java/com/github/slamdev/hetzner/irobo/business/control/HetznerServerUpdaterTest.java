package com.github.slamdev.hetzner.irobo.business.control;

import com.github.slamdev.hetzner.irobo.business.boundary.HetznerServerProducer;
import com.github.slamdev.hetzner.irobo.business.entity.HetznerServerMessage;
import com.github.slamdev.hetzner.irobo.integration.HetznerApi;
import com.google.common.net.InetAddresses;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HetznerServerUpdaterTest {

    @InjectMocks
    private HetznerServerUpdater hetznerServerUpdater;

    @Mock
    private HetznerApi hetznerApi;

    @Mock
    private HetznerServerProducer hetznerServerProducer;

    @Captor
    private ArgumentCaptor<HetznerServerMessage> serverCaptor;

    @Test
    @SuppressWarnings("UnstableApiUsage")
    void should_send_hetzner_servers() {
        List<HetznerApi.Server> servers = List.of(
                HetznerApi.Server.builder()
                        .serverNumber(1)
                        .serverName("srv1")
                        .product("p1")
                        .dc("dc1")
                        .serverIpV4(InetAddresses.forString("127.0.0.1"))
                        .serverIpV6(InetAddresses.forString("2001:0db8:85a3:0000:0000:8a2e:0370:7334"))
                        .cancelled(true)
                        .build(),
                HetznerApi.Server.builder()
                        .serverNumber(2)
                        .serverName("srv2")
                        .product("p2")
                        .dc("dc2")
                        .serverIpV4(InetAddresses.forString("192.168.0.1"))
                        .serverIpV6(InetAddresses.forString("2001:db8:3333:4444:CCCC:DDDD:EEEE:FFFF"))
                        .cancelled(false)
                        .build()
        );
        when(hetznerApi.getServers()).thenReturn(servers);

        hetznerServerUpdater.updateServers();

        verify(hetznerServerProducer, times(2)).sendHetznerServer(serverCaptor.capture());
        List<HetznerServerMessage> actual = serverCaptor.getAllValues();
        assertThat(actual).containsExactlyInAnyOrder(
                HetznerServerMessage.builder()
                        .id(servers.get(0).getServerNumber())
                        .name(servers.get(0).getServerName())
                        .product(servers.get(0).getProduct())
                        .dc(servers.get(0).getDc())
                        .ipV4(servers.get(0).getServerIpV4())
                        .ipV6(servers.get(0).getServerIpV6())
                        .cancelled(servers.get(0).isCancelled())
                        .build(),
                HetznerServerMessage.builder()
                        .id(servers.get(1).getServerNumber())
                        .name(servers.get(1).getServerName())
                        .product(servers.get(1).getProduct())
                        .dc(servers.get(1).getDc())
                        .ipV4(servers.get(1).getServerIpV4())
                        .ipV6(servers.get(1).getServerIpV6())
                        .cancelled(servers.get(1).isCancelled())
                        .build()
        );
    }
}
