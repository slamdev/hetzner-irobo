package com.github.slamdev.hetzner.irobo.business.control;

import com.github.slamdev.hetzner.irobo.business.entity.ServerModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ServerSearchKeywordsBuilder {

    public String getSearchKeywords(ServerModel model) {
        return merge(
                model.getId(),
                model.getName(),
                model.getProduct(),
                model.getDc(),
                Optional.ofNullable(model.getIpV4()).map(InetAddress::getHostAddress).orElse(null),
                Optional.ofNullable(model.getIpV6()).map(InetAddress::getHostAddress).orElse(null),
                model.getZabbixHostId(),
                model.getZabbixHost(),
                Optional.ofNullable(model.getZabbixIp()).map(InetAddress::getHostAddress).orElse(null),
                String.join(" ", model.getTags())
        );
    }

    private String merge(Object... fields) {
        List<String> strings = Arrays.stream(fields)
                .filter(Objects::nonNull)
                .map(Object::toString)
                .collect(Collectors.toList());
        return String.join(" ", strings);
    }
}
