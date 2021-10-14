package com.github.slamdev.hetzner.irobo.business.control;

import com.github.slamdev.hetzner.irobo.integration.SshClientExecutor;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class ServerTagsDetector {

    final List<Detector> detectors = List.of(
            new Detector("crictl ps", (stdOut, tags) -> {
                if (stdOut.contains("kube-apiserver")) {
                    tags.add("kube");
                    tags.add("kube-master");
                } else if (stdOut.contains("kube-proxy")) {
                    tags.add("kube");
                    tags.add("kube-worker");
                }
            }),
            new Detector("docker ps", (stdOut, tags) -> {
                if (stdOut.contains("rancher")) {
                    tags.add("rancher");
                }
            }),
            new Detector("ps x -eo comm", (stdOut, tags) -> {
                if (stdOut.contains("ceph-osd")) {
                    tags.add("ceph-osd");
                } else if (stdOut.contains("ceph-mon")) {
                    tags.add("ceph-mon");
                } else if (stdOut.contains("mysqld")) {
                    tags.add("mysql");
                } else if (stdOut.contains("pf purge")) {
                    tags.add("pfsense");
                } else if (stdOut.contains("kvm-pit")) {
                    tags.add("opennebula-node");
                } else if (stdOut.contains("postgres")) {
                    tags.add("postgres");
                }
            })
    );

    private final SshClientExecutor ssh;

    public List<String> getServerTags(InetAddress ip) {
        try {
            return detectTags(ip);
        } catch (IOException e) {
            log.error("failed to detect tags for " + ip.getHostAddress(), e);
            return Collections.emptyList();
        }
    }

    private List<String> detectTags(InetAddress address) throws IOException {
        List<String> tags = new ArrayList<>();
        for (Detector detector : detectors) {
            ssh.exec(address, session -> {
                Session.Command cmd = session.exec(detector.getCommand());
                String errorStream = IOUtils.readFully(cmd.getErrorStream()).toString();
                if (cmd.getExitStatus() == null) {
                    log.error("something went wrong for {}", address.getHostAddress());
                    throw new IOException("something went wrong for " + address.getHostAddress());
                }
                if (cmd.getExitStatus().equals(0)) {
                    String inputStream = IOUtils.readFully(cmd.getInputStream()).toString();
                    detector.getAction().accept(inputStream, tags);
                } else {
                    log.debug("command {} failed with {}: {}", detector.getCommand(), cmd.getExitStatus(), errorStream.replaceAll("\\R+", ""));
                }
            });
            if (!tags.isEmpty()) {
                return tags;
            }
        }
        return Collections.emptyList();
    }

    @Value
    private static class Detector {
        String command;
        BiConsumer<String, List<String>> action;
    }
}
