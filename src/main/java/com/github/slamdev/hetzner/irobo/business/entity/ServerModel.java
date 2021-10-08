package com.github.slamdev.hetzner.irobo.business.entity;

import lombok.Builder;
import lombok.Value;
import lombok.With;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

import java.net.InetAddress;
import java.time.Instant;
import java.time.LocalDate;

@Value
@With
@Builder
@Table("server")
public class ServerModel {

    @Id
    int serverNumber;

    InetAddress serverIpV4;

    InetAddress serverIpV6;

    String serverName;

    String product;

    String dc;

    String traffic;

    Status status;

    boolean cancelled;

    LocalDate paidUntil;

    InetAddress[] ips;

    Subnet[] subnets;

    Integer linkedStorageBox;

    Integer zabbixHostId;

    String zabbixHost;

    InetAddress zabbixIp;

    String[] tags;

    @Version
    Long version;

    @CreatedDate
    Instant createdDate;

    @LastModifiedDate
    Instant lastModifiedDate;

    public enum Status {
        READY,
        IN_PROCESS,
    }

    @Value
    @With
    @Builder
    public static class Subnet {
        InetAddress ip;
        int mask;
    }
}
