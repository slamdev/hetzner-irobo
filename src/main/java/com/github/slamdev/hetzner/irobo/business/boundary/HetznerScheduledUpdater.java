package com.github.slamdev.hetzner.irobo.business.boundary;

import com.github.slamdev.hetzner.irobo.business.control.HetznerServerUpdater;
import com.github.slamdev.hetzner.irobo.business.control.UpdateHistoryRepository;
import com.github.slamdev.hetzner.irobo.business.entity.UpdateHistoryModel;
import com.github.slamdev.hetzner.irobo.integration.AppConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.github.slamdev.hetzner.irobo.business.entity.UpdateHistoryModel.UpdateType.HETZNER_SERVER_LIST;

@Slf4j
@Component
@RequiredArgsConstructor
public class HetznerScheduledUpdater {

    private final HetznerServerUpdater hetznerServerUpdater;
    private final UpdateHistoryRepository updateHistoryRepository;
    private final AppConfig appConfig;

    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES, initialDelay = 1)
    @SchedulerLock(name = "hetzner-servers-list", lockAtLeastFor = "10s", lockAtMostFor = "30s")
    public void scheduleUpdateServersList() {
        Optional<UpdateHistoryModel> history = updateHistoryRepository.findFirstByUpdateTypeOrderByExecutedDateDesc(HETZNER_SERVER_LIST);
        boolean updateRequired = history
                .map(UpdateHistoryModel::getExecutedDate)
                .map(d -> d.plus(appConfig.getUpdateInterval().getHetznerServersList()).isBefore(Instant.now()))
                .orElse(true);

        if (!updateRequired) {
            log.debug("last update {} is newer than {}; no action will be performed", history, appConfig.getUpdateInterval().getHetznerServersList());
            return;
        }

        hetznerServerUpdater.updateServers();

        UpdateHistoryModel updatedHistory = updateHistoryRepository.save(UpdateHistoryModel.builder()
                .updateType(HETZNER_SERVER_LIST)
                .executedDate(Instant.now())
                .build());

        log.info("servers list updated: {}", updatedHistory);
    }
}
