package com.github.slamdev.hetzner.irobo.business.boundary;

import com.github.slamdev.hetzner.irobo.business.control.ServerFactory;
import com.github.slamdev.hetzner.irobo.business.control.ServerRepository;
import com.github.slamdev.hetzner.irobo.business.entity.HetznerServerMessage;
import com.github.slamdev.hetzner.irobo.business.entity.ServerModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HetznerServerConsumerTest {

    @InjectMocks
    private HetznerServerConsumer hetznerServerConsumer;

    @Mock
    private ServerRepository serverRepository;

    @Mock
    private ServerFactory serverFactory;

    @Test
    void should_update_active_server() {
        HetznerServerMessage msg = HetznerServerMessage.builder().id(1).cancelled(false).build();
        ServerModel server = ServerModel.builder().id(msg.getId()).build();
        when(serverFactory.create(msg)).thenReturn(server);

        hetznerServerConsumer.accept(msg);

        verify(serverRepository, times(1)).save(server);
    }

    @Test
    void should_delete_canceled_server() {
        HetznerServerMessage msg = HetznerServerMessage.builder().id(1).cancelled(true).build();

        hetznerServerConsumer.accept(msg);

        verify(serverRepository, times(1)).deleteById(msg.getId());
    }
}
