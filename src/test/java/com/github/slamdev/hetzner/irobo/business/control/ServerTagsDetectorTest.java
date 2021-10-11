package com.github.slamdev.hetzner.irobo.business.control;

import com.github.slamdev.hetzner.irobo.integration.SshClientExecutor;
import com.google.common.net.InetAddresses;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServerTagsDetectorTest {

    @InjectMocks
    private ServerTagsDetector serverTagsDetector;

    @Mock
    private SshClientExecutor ssh;

    @Captor
    private ArgumentCaptor<InetAddress> ipCaptor;

    @Test
    @SuppressWarnings("UnstableApiUsage")
    void should_detect_tags() throws IOException {
        List<InetAddress> ips = List.of(
                InetAddresses.forString("127.0.0.1"),
                InetAddresses.forString("192.168.0.1")
        );

        List<String> tags = serverTagsDetector.getServerTags(ips);

        verify(ssh, times(ips.size() * serverTagsDetector.detectors.size()))
                .exec(ipCaptor.capture(), any());
        assertThat(tags).isNotNull();
        assertThat(ipCaptor.getAllValues()).containsOnly(ips.toArray(InetAddress[]::new));
    }

    @Test
    @SuppressWarnings("UnstableApiUsage")
    void should_add_no_ssh_tag_on_exception() throws IOException {
        List<InetAddress> ips = List.of(
                InetAddresses.forString("127.0.0.1"),
                InetAddresses.forString("192.168.0.1")
        );
        doThrow(IOException.class).when(ssh).exec(any(), any());

        List<String> tags = serverTagsDetector.getServerTags(ips);

        assertThat(tags).containsOnly("no-ssh");

        tags = serverTagsDetector.getServerTags(ips, "tag");

        assertThat(tags).containsOnly("no-ssh", "tag");
    }
}
