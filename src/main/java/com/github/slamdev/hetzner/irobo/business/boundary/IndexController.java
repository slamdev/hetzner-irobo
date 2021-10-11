package com.github.slamdev.hetzner.irobo.business.boundary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.slamdev.hetzner.irobo.business.control.ServerRepository;
import com.github.slamdev.hetzner.irobo.business.entity.ServerListViewModel;
import com.github.slamdev.hetzner.irobo.business.entity.ServerModel;
import com.github.slamdev.hetzner.irobo.integration.AppConfig;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class IndexController {

    private final ServerRepository serverRepository;
    private final ObjectMapper objectMapper;
    private final AppConfig appConfig;

    @GetMapping
    public ModelAndView indexPage(@RequestParam(defaultValue = "id") String sortField,
                                  @RequestParam(defaultValue = "ASC") Sort.Direction sortDirection,
                                  @RequestParam(defaultValue = "") String filter) {
        String[] fields;
        switch (sortField) {
            case "externalIp":
                fields = new String[]{"ipV4", "ipV6"};
                break;
            case "internalIp":
                fields = new String[]{"zabbixIp"};
                break;
            case "hostName":
                fields = new String[]{"zabbixHost"};
                break;
            default:
                fields = new String[]{sortField};
                break;
        }
        Sort sort = Sort.by(sortDirection, fields);
        List<ServerModel> results;
        if (filter.trim().isEmpty()) {
            results = serverRepository.findAll(sort);
        } else {
            String searchKeywords = "%" + filter.trim().toLowerCase(Locale.ROOT) + "%";
            results = serverRepository.findAllBySearchKeywordsLike(searchKeywords, sort);
        }
        List<ServerListViewModel> servers = results.stream()
                .map(s -> ServerListViewModel.builder()
                        .id(s.getId())
                        .name(s.getName())
                        .hostName(s.getZabbixHost())
                        .externalIp(s.getIpV4() == null ? s.getIpV6() : s.getIpV4())
                        .internalIp(s.getZabbixIp())
                        .product(s.getProduct())
                        .dc(s.getDc())
                        .hetznerUrl(buildHetznerUrl(s))
                        .zabbixUrl(buildZabbixUrl(s))
                        .tags(s.getTags() == null ? Collections.emptyList() : Arrays.asList(s.getTags()))
                        .build())
                .collect(Collectors.toList());
        return new ModelAndView("pages/index", Map.of("servers", servers));
    }

    private String buildZabbixUrl(ServerModel server) {
        if (server.getZabbixHostId() == null) {
            return null;
        }
        return appConfig.getZabbix().getUrl() + "/hostinventories.php?hostid=" + server.getZabbixHostId();
    }

    private String buildHetznerUrl(ServerModel server) {
        return appConfig.getRobot().getWebUrl() + "/server?text=" + server.getId();
    }

    @GetMapping("details")
    @SneakyThrows
    public ModelAndView detailsPage(@RequestParam int id) {
        ServerModel server = serverRepository.findById(id).orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND));
        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(server);
        return new ModelAndView("pages/details", Map.of("json", json));
    }
}
