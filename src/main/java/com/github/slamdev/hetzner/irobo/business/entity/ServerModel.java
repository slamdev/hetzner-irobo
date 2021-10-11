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

@Value
@With
@Builder(toBuilder = true)
@Table("server")
public class ServerModel {
    @Id
    int id;
    String name;
    String product;
    String dc;
    InetAddress ipV4;
    InetAddress ipV6;
    Integer zabbixHostId;
    String zabbixHost;
    InetAddress zabbixIp;
    String searchKeywords;
    String[] tags;
    @Version
    Long version;
    @CreatedDate
    Instant createdDate;
    @LastModifiedDate
    Instant lastModifiedDate;
}
