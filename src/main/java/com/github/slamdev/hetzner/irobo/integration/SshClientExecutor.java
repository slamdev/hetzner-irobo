package com.github.slamdev.hetzner.irobo.integration;

import com.google.mu.function.CheckedConsumer;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetAddress;

@Component
@RequiredArgsConstructor
public class SshClientExecutor {

    private final AppConfig appConfig;


    @Retryable(include = IOException.class)
    @SneakyThrows
    public void exec(InetAddress ip, CheckedConsumer<Session, IOException> consumer) {
        try (SSHClient ssh = new SSHClient()) {
            KeyProvider keyProvider = ssh.loadKeys(appConfig.getRobot().getSshKey(), null, null);
            ssh.addHostKeyVerifier((hostname, port, key) -> true);
            ssh.connect(ip);
            ssh.authPublickey("root", keyProvider);
            try (Session session = ssh.startSession()) {
                consumer.accept(session);
            }
        }
    }
}
