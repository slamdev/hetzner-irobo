package com.github.slamdev.hetzner.irobo.business.boundary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.slamdev.hetzner.irobo.business.control.ServerRepository;
import com.github.slamdev.hetzner.irobo.business.control.ServerSearchRepository;
import com.github.slamdev.hetzner.irobo.business.entity.ServerListViewModel;
import com.github.slamdev.hetzner.irobo.business.entity.ServerModel;
import com.github.slamdev.hetzner.irobo.integration.AppConfig;
import com.github.slamdev.hetzner.irobo.integration.Streams;
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

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class IndexController {

    private final ServerRepository serverRepository;
    private final ServerSearchRepository serverSearchRepository;
    private final ObjectMapper objectMapper;
    private final AppConfig appConfig;

    @GetMapping
    public ModelAndView indexPage(@RequestParam(defaultValue = "serverNumber") String sortField,
                                  @RequestParam(defaultValue = "ASC") Sort.Direction sortDirection,
                                  @RequestParam(defaultValue = "") String filter) {
        String[] fields;
        switch (sortField) {
            case "externalIp":
                fields = new String[]{"serverIpV4", "serverIpV6"};
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
        Iterable<ServerModel> results;
        if (filter.trim().isEmpty()) {
            results = serverRepository.findAll(sort);
        } else {
            List<Integer> ids = serverSearchRepository.findAllBySearchDataLike("%" + filter.toLowerCase(Locale.ROOT) + "%");
            results = serverRepository.findAllByServerNumberIsIn(ids, sort);
        }
        List<ServerListViewModel> servers = Streams.stream(results)
                .map(s -> ServerListViewModel.builder()
                        .serverNumber(s.getServerNumber())
                        .serverName(s.getServerName())
                        .hostName(s.getZabbixHost())
                        .externalIp(s.getServerIpV4() == null ? s.getServerIpV6() : s.getServerIpV4())
                        .internalIp(s.getZabbixIp())
                        .product(s.getProduct())
                        .dc(s.getDc())
                        .hetznerUrl(buildHetznerUrl(s))
                        .zabbixUrl(buildZabbixUrl(s))
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
        return appConfig.getRobot().getWebUrl() + "/server?text=" + server.getServerNumber();
    }

    @GetMapping("details")
    @SneakyThrows
    public ModelAndView detailsPage(@RequestParam int id) {
        ServerModel server = serverRepository.findById(id).orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND));
        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(server);
        return new ModelAndView("pages/details", Map.of("json", json));
    }
}
