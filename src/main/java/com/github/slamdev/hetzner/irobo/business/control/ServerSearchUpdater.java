package com.github.slamdev.hetzner.irobo.business.control;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.slamdev.hetzner.irobo.business.entity.ServerModel;
import com.github.slamdev.hetzner.irobo.business.entity.ServerSearchModel;
import com.github.slamdev.hetzner.irobo.business.entity.UpdateHistoryModel;
import com.github.slamdev.hetzner.irobo.integration.AppConfig;
import com.github.slamdev.hetzner.irobo.integration.Streams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ServerSearchUpdater {

    private final ServerRepository serverRepository;
    private final ServerSearchRepository serverSearchRepository;
    private final UpdateHistoryRepository updateHistoryRepository;
    private final ObjectMapper objectMapper;
    private final AppConfig appConfig;

    @Scheduled(fixedDelay = 60, timeUnit = TimeUnit.SECONDS, initialDelay = 20)
    @SchedulerLock(name = "server-search", lockAtLeastFor = "10s", lockAtMostFor = "30s")
    public void scheduleUpdateServerSearch() {
        Optional<UpdateHistoryModel> update = updateHistoryRepository.findFirstByUpdateTypeOrderByExecutedDateDesc(UpdateHistoryModel.UpdateType.SERVER_SEARCH);
        boolean updateRequired = update
                .map(UpdateHistoryModel::getExecutedDate)
                .map(d -> d.plus(appConfig.getUpdateInterval().getServerSearch()).isBefore(Instant.now()))
                .orElse(true);
        if (updateRequired) {
            log.info("triggering scheduled update because last update {} is older than {}", update, appConfig.getUpdateInterval().getServerSearch());
            updateServerSearch();
        } else {
            log.debug("last update {} is newer than {}", update, appConfig.getUpdateInterval().getServerSearch());
        }
    }

    public void updateServerSearch() {
        Map<Integer, ServerSearchModel> existingSearches = Streams.stream(serverSearchRepository.findAll())
                .collect(Collectors.toMap(ServerSearchModel::getServerNumber, Function.identity()));

        List<ServerModel> servers = serverRepository.findAll();

        Set<Integer> deletedSearches = detectDeletedSearches(existingSearches.keySet(), servers);
        serverSearchRepository.deleteAllById(deletedSearches);

        List<ServerSearchModel> searches = servers.stream()
                .map(s -> existingSearches.containsKey(s.getServerNumber()) ? mergeSearches(existingSearches.get(s.getServerNumber()), s) : createSearch(s))
                .collect(Collectors.toList());

        serverSearchRepository.saveAll(searches);

        UpdateHistoryModel update = UpdateHistoryModel.builder()
                .updateType(UpdateHistoryModel.UpdateType.SERVER_SEARCH)
                .executedDate(Instant.now())
                .build();
        updateHistoryRepository.save(update);

        log.info("executed update {} for {} servers", update, servers.size());
    }

    private Set<Integer> detectDeletedSearches(Set<Integer> storedSearchIds, List<ServerModel> servers) {
        Set<Integer> searchesToDelete = new HashSet<>(storedSearchIds);
        servers.stream().map(ServerModel::getServerNumber).forEach(searchesToDelete::remove);
        return searchesToDelete;
    }

    private ServerSearchModel createSearch(ServerModel dto) {
        return ServerSearchModel.builder()
                .serverNumber(dto.getServerNumber())
                .searchData(toSearchData(dto))
                .build();
    }

    private ServerSearchModel mergeSearches(ServerSearchModel existing, ServerModel update) {
        return existing.withSearchData(toSearchData(update));
    }

    @SuppressWarnings("unchecked")
    private String toSearchData(ServerModel server) {
        Map<String, Object> data = objectMapper.convertValue(server, Map.class);
        StringBuilder sb = new StringBuilder();
        appendSearchData(sb, data.values());
        return sb.toString().trim();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void appendSearchData(StringBuilder sb, Object o) {
        if (o instanceof Map) {
            ((Map) o).forEach((key, value) -> appendSearchData(sb, value));
        } else if (o instanceof Collection) {
            ((Collection) o).forEach(e -> appendSearchData(sb, e));
        } else if (o != null && !o.toString().trim().isEmpty()) {
            sb.append(o.toString().trim().toLowerCase(Locale.ROOT)).append(' ');
        }
    }
}
