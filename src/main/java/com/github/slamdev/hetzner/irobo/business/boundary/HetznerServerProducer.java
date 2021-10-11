package com.github.slamdev.hetzner.irobo.business.boundary;

import com.github.slamdev.hetzner.irobo.business.entity.HetznerServerMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HetznerServerProducer {

    private static final String DESTINATION = "hetznerServerProducer-out-0";
    private final StreamBridge streamBridge;

    public void sendHetznerServer(HetznerServerMessage server) {
        streamBridge.send(DESTINATION, server);
    }
}
