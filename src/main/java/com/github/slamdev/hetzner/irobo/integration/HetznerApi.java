package com.github.slamdev.hetzner.irobo.integration;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class HetznerApi {

    private static final String RACKSPOT_PRODUCT = "Rackspot Reservation";
    private final RestTemplate template;

    public HetznerApi(RestTemplateBuilder builder, AppConfig appConfig) {
        template = builder
                .rootUri(appConfig.getRobot().getApiUrl())
                .basicAuthentication(appConfig.getRobot().getUsername(), appConfig.getRobot().getPassword())
                .build();
    }

    public List<Server> getServers() {
        Response[] responses = template.getForObject("/server", Response[].class);
        return Arrays.stream(responses)
                .map(Response::getServer)
                .filter(s -> !RACKSPOT_PRODUCT.equals(s.getProduct()))
                .collect(Collectors.toList());
    }

    @Value
    @Builder
    @Jacksonized
    private static class Response {
        @JsonProperty("server")
        Server server;
    }

    @Value
    @Builder
    @Jacksonized
    public static class Server {
        @JsonProperty("server_ip")
        InetAddress serverIpV4;
        @JsonProperty("server_ipv6_net")
        InetAddress serverIpV6;
        @JsonProperty("server_number")
        int serverNumber;
        @JsonProperty("server_name")
        String serverName;
        String product;
        String dc;
        String traffic;
        Status status;
        boolean cancelled;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        @JsonProperty("paid_until")
        LocalDate paidUntil;
        @JsonProperty("ip")
        List<InetAddress> ips;
        @JsonProperty("subnet")
        List<Subnet> subnets;
        @JsonProperty("linked_storagebox")
        Integer linkedStoragebox;

        public enum Status {
            @JsonProperty("ready")
            READY,
            @JsonProperty("in process")
            IN_PROCESS,
        }

        @Value
        @Builder
        @Jacksonized
        public static class Subnet {
            InetAddress ip;
            byte mask;
        }
    }
}
