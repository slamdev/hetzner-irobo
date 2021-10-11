package com.github.slamdev.hetzner.irobo.integration;

import com.google.mu.function.CheckedConsumer;
import lombok.RequiredArgsConstructor;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class SshClientExecutor {

    private final AppConfig appConfig;

    public void exec(InetAddress ip, CheckedConsumer<Session, IOException> consumer) throws IOException {
        try (SSHClient ssh = new SSHClient()) {
            KeyProvider keyProvider = ssh.loadKeys(appConfig.getRobot().getSshKey(), null, null);
            ssh.addHostKeyVerifier((hostname, port, key) -> true);
            ssh.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(3));
            ssh.connect(ip);
            ssh.authPublickey("root", keyProvider);
            try (Session session = ssh.startSession()) {
                consumer.accept(session);
            }
        }
    }
}
