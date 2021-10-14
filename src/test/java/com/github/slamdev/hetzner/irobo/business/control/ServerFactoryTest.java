package com.github.slamdev.hetzner.irobo.business.control;

import com.github.slamdev.hetzner.irobo.business.entity.HetznerServerMessage;
import com.github.slamdev.hetzner.irobo.business.entity.ServerModel;
import com.github.slamdev.hetzner.irobo.integration.ZabbixApi;
import com.google.common.net.InetAddresses;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServerFactoryTest {

    @InjectMocks
    private ServerFactory serverFactory;

    @Mock
    private ServerRepository serverRepository;

    @Mock
    private ZabbixApi zabbixApi;

    @Mock
    private ServerTagsDetector serverTagsDetector;

    @Mock
    private ServerSearchKeywordsBuilder serverSearchKeywordsBuilder;

    @Test
    @SuppressWarnings("UnstableApiUsage")
    void should_create_server() {
        HetznerServerMessage msg = HetznerServerMessage.builder()
                .id(1)
                .name("test")
                .product("p")
                .dc("d")
                .ipV4(InetAddresses.forString("127.0.0.1"))
                .ipV6(InetAddresses.forString("2001:0db8:85a3:0000:0000:8a2e:0370:7334"))
                .build();

        when(serverRepository.findById(msg.getId())).thenReturn(Optional.of(ServerModel.builder()
                .id(msg.getId())
                .version(1L)
                .build()));

        ZabbixApi.Host host = ZabbixApi.Host.builder()
                .hostId(2)
                .hostName("h")
                .ip(InetAddresses.forString("192.168.1.1"))
                .build();
        when(zabbixApi.getHost(msg.getId())).thenReturn(Optional.of(host));

        String searchKeywords = "1 2 3";
        when(serverSearchKeywordsBuilder.getSearchKeywords(any())).thenReturn(searchKeywords);

        List<String> tags = List.of("1", "2");
        when(serverTagsDetector.getServerTags(host.getIp())).thenReturn(tags);

        ServerModel actual = serverFactory.create(msg);

        assertThat(actual.getId()).isEqualTo(msg.getId());
        assertThat(actual.getName()).isEqualTo(msg.getName());
        assertThat(actual.getProduct()).isEqualTo(msg.getProduct());
        assertThat(actual.getDc()).isEqualTo(msg.getDc());
        assertThat(actual.getIpV4()).isEqualTo(msg.getIpV4());
        assertThat(actual.getIpV6()).isEqualTo(msg.getIpV6());
        assertThat(actual.getZabbixHostId()).isEqualTo(host.getHostId());
        assertThat(actual.getZabbixHost()).isEqualTo(host.getHostName());
        assertThat(actual.getZabbixIp()).isEqualTo(host.getIp());
        assertThat(actual.getSearchKeywords()).isEqualTo(searchKeywords);
        assertThat(actual.getTags()).containsExactlyElementsOf(tags);
        assertThat(actual.getVersion()).isEqualTo(1L);
    }
}
