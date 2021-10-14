package com.github.slamdev.hetzner.irobo.business.control;

import com.github.slamdev.hetzner.irobo.integration.SshClientExecutor;
import com.google.common.net.InetAddresses;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    @Test
    @SuppressWarnings("UnstableApiUsage")
    void should_detect_tags() throws IOException {
        InetAddress ip = InetAddresses.forString("127.0.0.1");
        List<String> tags = serverTagsDetector.getServerTags(ip);
        verify(ssh, times(serverTagsDetector.detectors.size())).exec(eq(ip), any());
        assertThat(tags).isNotNull();
    }
}
