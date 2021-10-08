package com.github.slamdev.hetzner.irobo.business.control;

import com.github.slamdev.hetzner.irobo.business.entity.ServerModel;
import com.github.slamdev.hetzner.irobo.business.entity.UpdateHistoryModel;
import com.github.slamdev.hetzner.irobo.integration.AppConfig;
import com.github.slamdev.hetzner.irobo.integration.SshClientExecutor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ServerTagsUpdater {

    private final SshClientExecutor ssh;
    private final UpdateHistoryRepository updateHistoryRepository;
    private final ServerRepository serverRepository;
    private final AppConfig appConfig;

    private final Map<String, BiConsumer<String, List<String>>> detectors = Map.of(
            "crictl ps", (stdOut, tags) -> {
                if (stdOut.contains("kube-apiserver")) {
                    tags.add("kube");
                    tags.add("kube-master");
                } else if (stdOut.contains("kube-proxy")) {
                    tags.add("kube");
                    tags.add("kube-worker");
                }
            },
            "ps --no-headers -eo comm", (stdOut, tags) -> {
                if (stdOut.contains("ceph-osd")) {
                    tags.add("ceph-osd");
                } else if (stdOut.contains("ceph-mon")) {
                    tags.add("ceph-mon");
                }
            },
            "docker ps", (stdOut, tags) -> {
                if (stdOut.contains("rancher/rancher-agent")) {
                    tags.add("rancher");
                }
            }
    );

    @Scheduled(fixedDelay = 10, timeUnit = TimeUnit.MINUTES, initialDelay = 2)
    @SchedulerLock(name = "servers-tags", lockAtLeastFor = "30s", lockAtMostFor = "10m")
    public void scheduleUpdateServerTags() {
        Optional<UpdateHistoryModel> update = updateHistoryRepository.findFirstByUpdateTypeOrderByExecutedDateDesc(UpdateHistoryModel.UpdateType.SERVER_TAGS);
        boolean updateRequired = update
                .map(UpdateHistoryModel::getExecutedDate)
                .map(d -> d.plus(appConfig.getUpdateInterval().getServerTags()).isBefore(Instant.now()))
                .orElse(true);
        if (updateRequired) {
            log.info("triggering scheduled update because last update {} is older than {}", update, appConfig.getUpdateInterval().getServerTags());
            updateServerTags();
        } else {
            log.debug("last update {} is newer than {}", update, appConfig.getUpdateInterval().getServerTags());
        }
    }

    @SneakyThrows
    public void updateServerTags() {
        List<ServerModel> servers = serverRepository.findAllByZabbixIpIsNotNull().stream()
                .map(s -> {
                    List<String> tags = Collections.emptyList();
                    try {
                        tags = detectTags(s.getZabbixIp());
                    } catch (Exception e) {
                        log.error("failed to detect tags for " + s.getZabbixIp(), e);
                    }
                    return s.withTags(tags.toArray(new String[0]));
                })
                .collect(Collectors.toList());
        serverRepository.saveAll(servers);

        UpdateHistoryModel update = UpdateHistoryModel.builder()
                .updateType(UpdateHistoryModel.UpdateType.SERVER_TAGS)
                .executedDate(Instant.now())
                .build();
        updateHistoryRepository.save(update);

        log.info("executed update {} for {} servers", update, servers.size());
    }

    private List<String> detectTags(InetAddress address) {
        List<String> tags = new ArrayList<>();
        for (Map.Entry<String, BiConsumer<String, List<String>>> entry : detectors.entrySet()) {
            ssh.exec(address, session -> {
                String cmdString = entry.getKey();
                BiConsumer<String, List<String>> action = entry.getValue();
                Session.Command cmd = session.exec(cmdString);
                String errorStream = IOUtils.readFully(cmd.getErrorStream()).toString();
                if (cmd.getExitStatus().equals(0)) {
                    String inputStream = IOUtils.readFully(cmd.getInputStream()).toString();
                    action.accept(inputStream, tags);
                } else {
                    log.debug("command {} failed with {}: {}", cmdString, cmd.getExitStatus(), errorStream.replaceAll("\\R+", ""));
                }
            });
        }
        if (tags.isEmpty()) {
            log.warn("failed to detect tags for {}", address);
        }
        return tags;
    }
}
