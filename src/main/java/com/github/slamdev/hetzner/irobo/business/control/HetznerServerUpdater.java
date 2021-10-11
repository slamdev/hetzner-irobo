package com.github.slamdev.hetzner.irobo.business.control;

import com.github.slamdev.hetzner.irobo.business.boundary.HetznerServerProducer;
import com.github.slamdev.hetzner.irobo.business.entity.HetznerServerMessage;
import com.github.slamdev.hetzner.irobo.integration.HetznerApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class HetznerServerUpdater {

    private final HetznerApi hetznerApi;
    private final HetznerServerProducer hetznerServerProducer;

    public void updateServers() {
        List<HetznerApi.Server> servers = hetznerApi.getServers();
        servers.stream()
                .map(this::toServerMessage)
                .forEach(hetznerServerProducer::sendHetznerServer);
    }

    private HetznerServerMessage toServerMessage(HetznerApi.Server server) {
        return HetznerServerMessage.builder()
                .id(server.getServerNumber())
                .name(server.getServerName())
                .product(server.getProduct())
                .dc(server.getDc())
                .ipV4(server.getServerIpV4())
                .ipV6(server.getServerIpV6())
                .cancelled(server.isCancelled())
                .build();
    }
}
