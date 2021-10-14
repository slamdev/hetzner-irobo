package com.github.slamdev.hetzner.irobo.business.control;

import com.github.slamdev.hetzner.irobo.business.entity.HetznerServerMessage;
import com.github.slamdev.hetzner.irobo.business.entity.ServerModel;
import com.github.slamdev.hetzner.irobo.integration.ZabbixApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ServerFactory {

    private final ServerRepository serverRepository;
    private final ZabbixApi zabbixApi;
    private final ServerTagsDetector serverTagsDetector;
    private final ServerSearchKeywordsBuilder serverSearchKeywordsBuilder;

    public ServerModel create(HetznerServerMessage msg) {
        ServerModel.ServerModelBuilder builder = serverRepository.findById(msg.getId())
                .map(ServerModel::toBuilder)
                .orElseGet(() -> ServerModel.builder().id(msg.getId()))
                .name(msg.getName())
                .product(msg.getProduct())
                .dc(msg.getDc())
                .ipV4(msg.getIpV4())
                .ipV6(msg.getIpV6());

        zabbixApi.getHost(msg.getId()).ifPresent(h -> builder.zabbixHostId(h.getHostId())
                .zabbixHost(h.getHostName())
                .zabbixIp(h.getIp()));

        List<String> tags = buildTags(builder.build());
        builder.tags(tags.toArray(String[]::new));

        String searchKeywords = serverSearchKeywordsBuilder.getSearchKeywords(builder.build());
        builder.searchKeywords(searchKeywords);

        return builder.build();
    }

    private List<String> buildTags(ServerModel model) {
        InetAddress validIp = model.getZabbixIp() == null ? model.getIpV4() : model.getZabbixIp();
        if (validIp == null) {
            return Collections.singletonList("no-ip");
        }
        List<String> tags = serverTagsDetector.getServerTags(validIp);
        if (tags.isEmpty()) {
            return Arrays.asList(model.getTags());
        }
        return tags;
    }
}
