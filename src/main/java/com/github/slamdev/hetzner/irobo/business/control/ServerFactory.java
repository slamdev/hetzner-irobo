package com.github.slamdev.hetzner.irobo.business.control;

import com.github.slamdev.hetzner.irobo.business.entity.HetznerServerMessage;
import com.github.slamdev.hetzner.irobo.business.entity.ServerModel;
import com.github.slamdev.hetzner.irobo.integration.ZabbixApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class ServerFactory {

    private final ServerRepository serverRepository;
    private final ZabbixApi zabbixApi;
    private final ServerTagsDetector serverTagsDetector;
    private final ServerSearchKeywordsBuilder serverSearchKeywordsBuilder;

    public ServerModel create(HetznerServerMessage msg) {
        List<InetAddress> ips = new ArrayList<>();
        Stream.of(msg.getIpV4(), msg.getIpV6()).filter(Objects::nonNull).forEach(ips::add);

        ServerModel.ServerModelBuilder builder = serverRepository.findById(msg.getId())
                .map(ServerModel::toBuilder)
                .orElseGet(() -> ServerModel.builder().id(msg.getId()))
                .name(msg.getName())
                .product(msg.getProduct())
                .dc(msg.getDc())
                .ipV4(msg.getIpV4())
                .ipV6(msg.getIpV6());

        zabbixApi.getHost(msg.getId()).ifPresent(h -> {
            builder.zabbixHostId(h.getHostId())
                    .zabbixHost(h.getHostName())
                    .zabbixIp(h.getIp());
            if (h.getIp() != null) {
                ips.add(h.getIp());
            }
        });

        List<String> tags = serverTagsDetector.getServerTags(ips, builder.build().getTags());
        builder.tags(tags.toArray(String[]::new));

        String searchKeywords = serverSearchKeywordsBuilder.getSearchKeywords(builder.build());
        builder.searchKeywords(searchKeywords);

        return builder.build();
    }
}
