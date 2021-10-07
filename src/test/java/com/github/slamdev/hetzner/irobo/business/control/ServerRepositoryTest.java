package com.github.slamdev.hetzner.irobo.business.control;

import com.github.slamdev.hetzner.irobo.business.entity.ServerModel;
import com.google.common.net.InetAddresses;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.CockroachContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.InetAddress;
import java.time.LocalDate;

@Slf4j
@Testcontainers
@Disabled
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@SuppressWarnings("UnstableApiUsage")
class ServerRepositoryTest {

    @Autowired
    private ServerRepository repository;

    @Container
    static CockroachContainer cockroachContainer = new CockroachContainer("cockroachdb/cockroach:v21.1.9");

    @DynamicPropertySource
    static void cockroachProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", cockroachContainer::getJdbcUrl);
        registry.add("spring.datasource.username", cockroachContainer::getUsername);
        registry.add("spring.datasource.password", cockroachContainer::getPassword);
    }

    @Test
    @SneakyThrows
    void should_find_all() {
        ServerModel entity = ServerModel.builder()
                .serverNumber(1)
                .serverName("test")
                .serverIpV4(InetAddresses.forString("10.1.1.1"))
                .serverIpV6(InetAddresses.forString("2001:0db8:85a3:0000:0000:8a2e:0370:7334"))
                .product("pr")
                .dc("dc1")
                .traffic("unlim")
                .status(ServerModel.Status.READY)
                .cancelled(true)
                .paidUntil(LocalDate.now())
                .ips(new InetAddress[]{InetAddresses.forString("192.168.1.1")})
                .subnets(new ServerModel.Subnet[]{
                        ServerModel.Subnet.builder()
                                .ip(InetAddresses.forString("8.8.1.1"))
                                .mask(1)
                                .build()
                })
                .build();
        repository.save(entity);

        Iterable<ServerModel> values = repository.findAll();
        log.info("{}", values);
    }
}

