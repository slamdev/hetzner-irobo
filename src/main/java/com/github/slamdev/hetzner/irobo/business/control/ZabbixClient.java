package com.github.slamdev.hetzner.irobo.business.control;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.slamdev.hetzner.irobo.business.entity.ZabbixHostDto;
import com.github.slamdev.hetzner.irobo.integration.AppConfig;
import io.github.mlniang.zabbix.client.request.ZabbixGetHostParams;
import io.github.mlniang.zabbix.client.response.JsonRPCResponse;
import io.github.mlniang.zabbix.client.service.ZabbixApiService;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ZabbixClient {

    private final String token;
    private final ZabbixApiService zabbixApi;
    private final ObjectMapper objectMapper;

    @SneakyThrows
    public ZabbixClient(ZabbixApiService zabbixApiService, AppConfig appConfig, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.zabbixApi = zabbixApiService;
        token = zabbixApiService.authenticate(appConfig.getZabbix().getUsername(), appConfig.getZabbix().getPassword());
    }

    @SneakyThrows
    public ZabbixHostDto[] getHosts() {
        ZabbixGetHostParams params = new ZabbixGetHostParams();
        params.setSelectInterfaces(List.of("interfaces", "ip"));
        params.setOutput(List.of("host"));
        JsonRPCResponse response = zabbixApi.call("host.get", params, token);
        return objectMapper.treeToValue(response.getResult(), ZabbixHostDto[].class);
    }
}
