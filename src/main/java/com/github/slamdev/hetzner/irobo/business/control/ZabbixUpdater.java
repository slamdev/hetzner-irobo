package com.github.slamdev.hetzner.irobo.business.control;

import com.github.slamdev.hetzner.irobo.business.entity.ServerModel;
import com.github.slamdev.hetzner.irobo.business.entity.UpdateHistoryModel;
import com.github.slamdev.hetzner.irobo.business.entity.ZabbixHostDto;
import com.github.slamdev.hetzner.irobo.integration.AppConfig;
import com.github.slamdev.hetzner.irobo.integration.Streams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ZabbixUpdater {

    private final ServerRepository serverRepository;
    private final UpdateHistoryRepository updateHistoryRepository;
    private final ZabbixClient zabbixClient;
    private final AppConfig appConfig;

    @Scheduled(fixedDelay = 60, timeUnit = TimeUnit.SECONDS, initialDelay = 10)
    @SchedulerLock(name = "zabbix-hosts-list", lockAtLeastFor = "10s", lockAtMostFor = "30s")
    public void scheduleUpdateHostsList() {
        Optional<UpdateHistoryModel> update = updateHistoryRepository.findFirstByUpdateTypeOrderByExecutedDateDesc(UpdateHistoryModel.UpdateType.ZABBIX_HOSTS_LIST);
        boolean updateRequired = update
                .map(UpdateHistoryModel::getExecutedDate)
                .map(d -> d.plus(appConfig.getUpdateInterval().getZabbixHostsList()).isBefore(Instant.now()))
                .orElse(true);
        if (updateRequired) {
            log.info("triggering scheduled update because last update {} is older than {}", update, appConfig.getUpdateInterval().getZabbixHostsList());
            updateHostsList();
        } else {
            log.debug("last update {} is newer than {}", update, appConfig.getUpdateInterval().getZabbixHostsList());
        }
    }

    @SuppressWarnings("PMD.NullAssignment")
    public void updateHostsList() {
        ZabbixHostDto[] hosts = zabbixClient.getHosts();

        List<ServerModel> servers = Streams.stream(serverRepository.findAll())
                .map(s -> {
                    Optional<ZabbixHostDto> zabbixHost = selectZabbixHost(s, hosts);
                    if (zabbixHost.isEmpty() && !s.isCancelled() && !"Rackspot Reservation".equals(s.getProduct())) {
                        log.warn("failed to detect zabbix host for {}", s.getServerNumber());
                    }
                    ZabbixHostDto zh = zabbixHost.orElseGet(() -> ZabbixHostDto.builder().build());
                    return s.withZabbixHost(zh.getHost())
                            .withZabbixIp(zh.getInterfaces() == null || zh.getInterfaces().isEmpty() ? null : zh.getInterfaces().get(0).getIp())
                            .withZabbixHostId(zh.getHostId() == null ? null : Integer.valueOf(zh.getHostId()));
                })
                .collect(Collectors.toList());

        serverRepository.saveAll(servers);

        UpdateHistoryModel update = UpdateHistoryModel.builder()
                .updateType(UpdateHistoryModel.UpdateType.ZABBIX_HOSTS_LIST)
                .executedDate(Instant.now())
                .build();
        updateHistoryRepository.save(update);

        log.info("executed update {} for {} hosts", update, hosts.length);
    }

    private Optional<ZabbixHostDto> selectZabbixHost(ServerModel server, ZabbixHostDto... hosts) {
        return Arrays.stream(hosts)
                .filter(h ->
                        h.getHost().contains("-" + server.getServerNumber() + "-")
                                || h.getHost().endsWith("-" + server.getServerNumber())
                )
                .findFirst();
    }
}
