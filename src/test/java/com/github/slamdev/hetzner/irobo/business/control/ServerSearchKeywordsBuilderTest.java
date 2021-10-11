package com.github.slamdev.hetzner.irobo.business.control;

import com.github.slamdev.hetzner.irobo.business.entity.ServerModel;
import com.google.common.net.InetAddresses;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ServerSearchKeywordsBuilderTest {

    private final ServerSearchKeywordsBuilder serverSearchKeywordsBuilder = new ServerSearchKeywordsBuilder();

    @Test
    @SuppressWarnings({"UnstableApiUsage", "StringBufferReplaceableByString"})
    void should_build_search_keywords() {
        ServerModel server = ServerModel.builder()
                .id(1)
                .name("srv")
                .product("p1")
                .dc("dc")
                .ipV4(InetAddresses.forString("127.0.0.1"))
                .ipV6(InetAddresses.forString("2001:0db8:85a3:0000:0000:8a2e:0370:7334"))
                .zabbixHostId(2)
                .zabbixHost("h1")
                .zabbixIp(InetAddresses.forString("192.168.0.1"))
                .searchKeywords("some-old-data")
                .tags(new String[]{"t1", "t2"})
                .version(2L)
                .createdDate(Instant.now())
                .lastModifiedDate(Instant.now())
                .build();

        String expected = new StringBuilder()
                .append(server.getId()).append(' ')
                .append(server.getName()).append(' ')
                .append(server.getProduct()).append(' ')
                .append(server.getDc()).append(' ')
                .append(server.getIpV4().getHostAddress()).append(' ')
                .append(server.getIpV6().getHostAddress()).append(' ')
                .append(server.getZabbixHostId()).append(' ')
                .append(server.getZabbixHost()).append(' ')
                .append(server.getZabbixIp().getHostAddress()).append(' ')
                .append(String.join(" ", server.getTags()))
                .toString();

        String actual = serverSearchKeywordsBuilder.getSearchKeywords(server);

        assertThat(actual).isEqualTo(expected);
    }
}
