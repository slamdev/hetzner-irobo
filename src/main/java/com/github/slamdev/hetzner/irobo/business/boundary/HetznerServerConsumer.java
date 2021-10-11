package com.github.slamdev.hetzner.irobo.business.boundary;

import com.github.slamdev.hetzner.irobo.business.control.ServerFactory;
import com.github.slamdev.hetzner.irobo.business.control.ServerRepository;
import com.github.slamdev.hetzner.irobo.business.entity.HetznerServerMessage;
import com.github.slamdev.hetzner.irobo.business.entity.ServerModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
@Component("hetznerServerConsumer")
public class HetznerServerConsumer implements Consumer<HetznerServerMessage> {

    private final ServerRepository serverRepository;
    private final ServerFactory serverFactory;

    @Override
    public void accept(HetznerServerMessage msg) {
        if (msg.isCancelled()) {
            serverRepository.deleteById(msg.getId());
            log.info("server {} is deleted because it is canceled", msg.getId());
            return;
        }

        ServerModel server = serverFactory.create(msg);
        server = serverRepository.save(server);

        log.info("server updated: {}", server);
    }
}
