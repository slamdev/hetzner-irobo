package com.github.slamdev.hetzner.irobo.business.control;

import com.github.slamdev.hetzner.irobo.business.entity.HetznerServerDto;
import com.github.slamdev.hetzner.irobo.business.entity.ServerModel;
import com.github.slamdev.hetzner.irobo.business.entity.UpdateHistoryModel;
import com.github.slamdev.hetzner.irobo.integration.AppConfig;
import com.github.slamdev.hetzner.irobo.integration.Streams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class HetznerUpdater {

    private final ServerRepository serverRepository;
    private final UpdateHistoryRepository updateHistoryRepository;
    private final HetznerRobotClient hetznerRobotClient;
    private final AppConfig appConfig;

    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
    @SchedulerLock(name = "hetzner-servers-list", lockAtLeastFor = "30s", lockAtMostFor = "1m")
    public void scheduleUpdateServersList() {
        Optional<UpdateHistoryModel> update = updateHistoryRepository.findFirstByUpdateTypeOrderByExecutedDateDesc(UpdateHistoryModel.UpdateType.HETZNER_SERVER_LIST);
        boolean updateRequired = update
                .map(UpdateHistoryModel::getExecutedDate)
                .map(d -> d.plus(appConfig.getUpdateInterval().getHetznerServersList()).isBefore(Instant.now()))
                .orElse(true);
        if (updateRequired) {
            log.info("triggering scheduled update because last update {} is older than {}", update, appConfig.getUpdateInterval().getHetznerServersList());
            updateServersList();
        } else {
            log.debug("last update {} is newer than {}", update, appConfig.getUpdateInterval().getHetznerServersList());
        }
    }

    public void updateServersList() {
        Map<Integer, ServerModel> existingServers = Streams.stream(serverRepository.findAll())
                .collect(Collectors.toMap(ServerModel::getServerNumber, Function.identity()));

        List<HetznerServerDto.Info> hetznerServers = hetznerRobotClient.getServers();

        Set<Integer> deletedServers = detectDeletedServers(existingServers.keySet(), hetznerServers);
        serverRepository.deleteAllById(deletedServers);

        List<ServerModel> servers = hetznerServers.stream()
                .map(s -> existingServers.containsKey(s.getServerNumber()) ? mergeServers(existingServers.get(s.getServerNumber()), s) : createServer(s))
                .collect(Collectors.toList());

        serverRepository.saveAll(servers);

        UpdateHistoryModel update = UpdateHistoryModel.builder()
                .updateType(UpdateHistoryModel.UpdateType.HETZNER_SERVER_LIST)
                .executedDate(Instant.now())
                .build();
        updateHistoryRepository.save(update);

        log.info("executed update {} for {} servers", update, hetznerServers.size());
    }

    private Set<Integer> detectDeletedServers(Set<Integer> storedServerIds, List<HetznerServerDto.Info> hetznerServers) {
        Set<Integer> serversToDelete = new HashSet<>(storedServerIds);
        hetznerServers.stream().map(HetznerServerDto.Info::getServerNumber).forEach(serversToDelete::remove);
        return serversToDelete;
    }

    private ServerModel createServer(HetznerServerDto.Info dto) {
        return ServerModel.builder()
                .serverNumber(dto.getServerNumber())
                .serverIpV4(dto.getServerIpV4())
                .serverIpV6(dto.getServerIpV6())
                .serverName(dto.getServerName())
                .product(dto.getProduct())
                .dc(dto.getDc())
                .traffic(dto.getTraffic())
                .status(ServerModel.Status.valueOf(dto.getStatus().name()))
                .cancelled(dto.isCancelled())
                .paidUntil(dto.getPaidUntil())
                .ips(Optional.ofNullable(dto.getIps()).orElseGet(ArrayList::new).toArray(InetAddress[]::new))
                .subnets(Optional.ofNullable(dto.getSubnets()).orElseGet(ArrayList::new).stream()
                        .map(net -> ServerModel.Subnet.builder()
                                .ip(net.getIp())
                                .mask(net.getMask())
                                .build())
                        .toArray(ServerModel.Subnet[]::new))
                .linkedStorageBox(dto.getLinkedStoragebox())
                .build();
    }

    private ServerModel mergeServers(ServerModel existing, HetznerServerDto.Info update) {
        return existing
                .withServerIpV4(update.getServerIpV4())
                .withServerIpV6(update.getServerIpV6())
                .withServerName(update.getServerName())
                .withProduct(update.getProduct())
                .withDc(update.getDc())
                .withTraffic(update.getTraffic())
                .withStatus(ServerModel.Status.valueOf(update.getStatus().name()))
                .withCancelled(update.isCancelled())
                .withPaidUntil(update.getPaidUntil())
                .withIps(Optional.ofNullable(update.getIps()).orElseGet(ArrayList::new).toArray(InetAddress[]::new))
                .withSubnets(Optional.ofNullable(update.getSubnets()).orElseGet(ArrayList::new).stream()
                        .map(net -> ServerModel.Subnet.builder()
                                .ip(net.getIp())
                                .mask(net.getMask())
                                .build())
                        .toArray(ServerModel.Subnet[]::new))
                .withLinkedStorageBox(update.getLinkedStoragebox());
    }
}
