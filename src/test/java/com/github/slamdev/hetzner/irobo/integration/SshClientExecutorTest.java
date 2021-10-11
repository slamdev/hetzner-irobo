package com.github.slamdev.hetzner.irobo.integration;

import com.google.common.net.InetAddresses;
import lombok.extern.slf4j.Slf4j;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@Slf4j
@Disabled
@DisableScheduler
@WithRequiredProperties
@AutoconfigureNoopDb
@AutoconfigureNoopBinder
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class SshClientExecutorTest {

    @Autowired
    SshClientExecutor ssh;

    @Test
    @SuppressWarnings("UnstableApiUsage")
    void should_execute_command() throws IOException {
        ssh.exec(InetAddresses.forString("10.3.4.155"), session -> {
            String cmdString = "ls -la";
            Session.Command cmd = session.exec(cmdString);
            String inputStream = IOUtils.readFully(cmd.getInputStream()).toString();
            String errorStream = IOUtils.readFully(cmd.getErrorStream()).toString();
            log.info("command finished with {} status", cmd.getExitStatus());
            log.info("stdout: \n{}", inputStream);
            log.info("stderr: \n{}", errorStream);
        });
    }
}
