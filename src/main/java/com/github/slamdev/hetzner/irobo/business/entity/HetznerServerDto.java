package com.github.slamdev.hetzner.irobo.business.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.net.InetAddress;
import java.time.LocalDate;
import java.util.List;

@Value
@Builder
@Jacksonized
public class HetznerServerDto {

    @JsonProperty("server")
    Info info;

    @Value
    @Builder
    @Jacksonized
    public static class Info {
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

        @Value
        @Builder
        @Jacksonized
        public static class Subnet {
            InetAddress ip;
            byte mask;
        }

        public enum Status {
            @JsonProperty("ready")
            READY,
            @JsonProperty("in process")
            IN_PROCESS,
        }
    }
}
