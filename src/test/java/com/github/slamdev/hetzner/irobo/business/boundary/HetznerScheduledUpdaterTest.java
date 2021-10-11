package com.github.slamdev.hetzner.irobo.business.boundary;

import com.github.slamdev.hetzner.irobo.business.control.HetznerServerUpdater;
import com.github.slamdev.hetzner.irobo.business.control.UpdateHistoryRepository;
import com.github.slamdev.hetzner.irobo.business.entity.UpdateHistoryModel;
import com.github.slamdev.hetzner.irobo.integration.AppConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static com.github.slamdev.hetzner.irobo.business.entity.UpdateHistoryModel.UpdateType.HETZNER_SERVER_LIST;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HetznerScheduledUpdaterTest {

    private final AppConfig appConfig = AppConfig.builder()
            .updateInterval(AppConfig.UpdateInterval.builder()
                    .hetznerServersList(Duration.of(1, ChronoUnit.HOURS))
                    .build())
            .build();
    private HetznerScheduledUpdater scheduledUpdater;
    @Mock
    private HetznerServerUpdater serverUpdater;
    @Mock
    private UpdateHistoryRepository updateHistoryRepository;

    @BeforeEach
    void beforeEach() {
        scheduledUpdater = new HetznerScheduledUpdater(serverUpdater, updateHistoryRepository, appConfig);
    }

    @Test
    void should_update_servers_when_no_history_found() {
        when(updateHistoryRepository.findFirstByUpdateTypeOrderByExecutedDateDesc(HETZNER_SERVER_LIST))
                .thenReturn(Optional.empty());
        scheduledUpdater.scheduleUpdateServersList();
        verify(serverUpdater, times(1)).updateServers();
        verify(updateHistoryRepository, times(1)).save(argThat(h ->
                HETZNER_SERVER_LIST == h.getUpdateType()
                        && h.getExecutedDate().isBefore(Instant.now())
                        && h.getExecutedDate().isAfter(Instant.now().minus(1, ChronoUnit.MINUTES))));
    }

    @Test
    void should_update_servers_when_history_is_older() {
        UpdateHistoryModel history = UpdateHistoryModel.builder()
                .executedDate(Instant.now().minus(appConfig.getUpdateInterval().getHetznerServersList()))
                .build();
        when(updateHistoryRepository.findFirstByUpdateTypeOrderByExecutedDateDesc(HETZNER_SERVER_LIST))
                .thenReturn(Optional.of(history));
        scheduledUpdater.scheduleUpdateServersList();
        verify(serverUpdater, times(1)).updateServers();
        verify(updateHistoryRepository, times(1)).save(argThat(h ->
                HETZNER_SERVER_LIST == h.getUpdateType()
                        && h.getExecutedDate().isBefore(Instant.now())
                        && h.getExecutedDate().isAfter(Instant.now().minus(1, ChronoUnit.MINUTES))));
    }

    @Test
    void should_do_nothing_when_history_is_newer() {
        UpdateHistoryModel history = UpdateHistoryModel.builder()
                .executedDate(Instant.now())
                .build();
        when(updateHistoryRepository.findFirstByUpdateTypeOrderByExecutedDateDesc(HETZNER_SERVER_LIST))
                .thenReturn(Optional.of(history));
        scheduledUpdater.scheduleUpdateServersList();
        verify(serverUpdater, times(0)).updateServers();
        verify(updateHistoryRepository, times(0)).save(any());
    }
}
