package com.github.slamdev.hetzner.irobo.business.entity;

import lombok.Builder;
import lombok.Value;

import java.net.InetAddress;

@Value
@Builder
public class HetznerServerMessage {
    int id;
    String name;
    String product;
    String dc;
    InetAddress ipV4;
    InetAddress ipV6;
    boolean cancelled;
}
