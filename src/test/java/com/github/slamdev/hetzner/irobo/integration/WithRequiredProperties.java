package com.github.slamdev.hetzner.irobo.integration;

import org.springframework.test.context.TestPropertySource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@TestPropertySource(properties = {
        "hetzner-irobo.robot.username=some",
        "hetzner-irobo.robot.password=some",
        "hetzner-irobo.robot.ssh-key=some",
        "hetzner-irobo.zabbix.url=some",
        "hetzner-irobo.zabbix.username=some",
        "hetzner-irobo.zabbix.password=some",
})
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface WithRequiredProperties {
}
