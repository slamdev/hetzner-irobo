package com.github.slamdev.hetzner.irobo.business.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.net.InetAddress;
import java.util.List;

@Value
@Builder
@Jacksonized
public class ZabbixHostDto {

    @JsonProperty("hostid")
    String hostId;
    String host;
    List<Interface> interfaces;

    @Value
    @Builder
    @Jacksonized
    public static class Interface {
        InetAddress ip;
    }
}
