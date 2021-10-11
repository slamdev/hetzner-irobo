package com.github.slamdev.hetzner.irobo.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Suppliers;
import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.util.*;
import java.util.function.Supplier;

@Slf4j
@Component
public class ZabbixApi {

    private static final String ENDPOINT = "/api_jsonrpc.php";
    private final AppConfig appConfig;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final Supplier<String> token = Suppliers.memoize(this::login);

    public ZabbixApi(RestTemplateBuilder builder, AppConfig appConfig, ObjectMapper objectMapper) {
        this.appConfig = appConfig;
        this.objectMapper = objectMapper;
        restTemplate = builder.rootUri(appConfig.getZabbix().getUrl())
                .defaultHeader("Content-Type", "application/json-rpc")
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    public Optional<Host> getHost(int hetznerServerNumber) {
        RpcRequest request = RpcRequest.builder()
                .auth(token.get())
                .method("host.get")
                .params(Map.of(
                        "output", Collections.singletonList("host"),
                        "selectInterfaces", List.of("interfaces", "ip"),
                        "search", Collections.singletonMap("host", hetznerServerNumber)
                )).build();
        RpcResponse response = restTemplate.postForObject(ENDPOINT, request, RpcResponse.class);
        HostResult[] hosts = objectMapper.convertValue(response.getResult(), HostResult[].class);
        if (hosts.length == 0) {
            log.warn("failed to get zabbix host by {} server number", hetznerServerNumber);
            return Optional.empty();
        } else if (hosts.length > 1) {
            log.warn("found {} hosts for {} server number: {}", hosts.length, hetznerServerNumber, hosts);
        }
        Host host = Host.builder()
                .hostId(hosts[0].getHostid())
                .hostName(hosts[0].getHost())
                .ip(hosts[0].getInterfaces() == null || hosts[0].getInterfaces().length == 0 ? null : hosts[0].getInterfaces()[0].getIp())
                .build();
        return Optional.of(host);
    }

    @SuppressWarnings("ConstantConditions")
    private String login() {
        RpcRequest request = RpcRequest.builder()
                .method("user.login")
                .params(Map.of(
                        "user", appConfig.getZabbix().getUsername(),
                        "password", appConfig.getZabbix().getPassword()
                )).build();
        RpcResponse response = restTemplate.postForObject(ENDPOINT, request, RpcResponse.class);
        return response.getResult().toString();
    }

    @Value
    @Builder
    private static class RpcRequest {
        @Builder.Default
        String jsonrpc = "2.0";
        String method;
        Map<String, Object> params;
        @Builder.Default
        String id = UUID.randomUUID().toString();
        String auth;
    }

    @Value
    @Builder
    private static class RpcResponse {
        Object result;
    }

    @Value
    @Builder
    private static class HostResult {
        int hostid;
        String host;
        Interface[] interfaces;

        @Value
        @Builder
        private static class Interface {
            InetAddress ip;
        }
    }

    @Value
    @Builder
    public static class Host {
        int hostId;
        String hostName;
        InetAddress ip;
    }
}
