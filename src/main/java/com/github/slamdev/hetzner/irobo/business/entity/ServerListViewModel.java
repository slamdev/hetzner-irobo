package com.github.slamdev.hetzner.irobo.business.entity;

import lombok.Builder;
import lombok.Value;

import java.net.InetAddress;

@Value
@Builder
public class ServerListViewModel {
    int serverNumber;
    String serverName;
    String hostName;
    InetAddress externalIp;
    InetAddress internalIp;
    String product;
    String dc;
    String hetznerUrl;
    String zabbixUrl;
}
