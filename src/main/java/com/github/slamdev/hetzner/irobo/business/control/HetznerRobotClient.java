package com.github.slamdev.hetzner.irobo.business.control;

import com.github.slamdev.hetzner.irobo.business.entity.HetznerServerDto;
import com.github.slamdev.hetzner.irobo.integration.AppConfig;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class HetznerRobotClient {

    private final RestTemplate template;

    @Valid
    public HetznerRobotClient(RestTemplateBuilder builder, AppConfig appConfig) {
        template = builder
                .rootUri(appConfig.getRobot().getUrl())
                .basicAuthentication(appConfig.getRobot().getUsername(), appConfig.getRobot().getPassword())
                .build();
    }

    @SneakyThrows
    public List<HetznerServerDto.Info> getServers() {
        HetznerServerDto[] servers = template.getForObject("/server", HetznerServerDto[].class);
        return Arrays.stream(servers)
                .map(HetznerServerDto::getInfo)
                .filter(s -> !"Rackspot Reservation".equals(s.getProduct()))
                .filter(s -> !s.isCancelled())
                .collect(Collectors.toList());
    }
}
