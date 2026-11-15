package cn.sichu.system.eda.executor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.password.PasswordFinder;
import net.schmizz.sshj.userauth.password.PasswordUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * SSH 执行器（sshj）。私钥来自部署配置（SshCredentialProvider），不经过业务层与数据库。
 * 骨架阶段首次连接信任主机指纹；生产可切换 known_hosts 校验。
 *
 * @author sichu huang
 * @since 2026/11/15
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SshRemoteExecutor implements RemoteExecutor {

    private final SshCredentialProvider credentialProvider;

    private final ExecutorService ioPool = Executors.newCachedThreadPool(runnable -> {
        Thread thread = new Thread(runnable, "ssh-exec-io");
        thread.setDaemon(true);
        return thread;
    });

    @Value("${ems.remote.max-output-bytes:65536}")
    private int maxOutputBytes;

    @Override
    public ExecResult exec(Target target, String command, String workdir, int timeoutSeconds) {
        int safeTimeout = Math.max(1, timeoutSeconds);
        Future<ExecResult> future = ioPool.submit(() -> run(target, command, workdir, safeTimeout));
        long overall = (long) safeTimeout + Math.max(1, target.connectTimeoutSeconds()) + 5L;
        try {
            return future.get(overall, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            return new ExecResult(-1, "", "执行超时（" + safeTimeout + "s）", true);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new ExecResult(-1, "", "执行被中断", true);
        } catch (ExecutionException e) {
            return new ExecResult(-1, "", rootMessage(e), false);
        }
    }

    private ExecResult run(Target target, String command, String workdir, int timeoutSeconds) {
        SSHClient ssh = new SSHClient();
        try {
            String privateKey = credentialProvider.requirePrivateKey();
            String username = credentialProvider.username(target.username());
            ssh.addHostKeyVerifier(new PromiscuousVerifier());
            ssh.setConnectTimeout(Math.max(1, target.connectTimeoutSeconds()) * 1000);
            ssh.connect(target.host(), target.port());
            String passphrase = credentialProvider.passphrase();
            PasswordFinder finder = passphrase == null ? null
                : PasswordUtils.createOneOff(passphrase.toCharArray());
            ssh.loadKeys(privateKey, null, finder);
            ssh.authPublickey(username);
            String wrapped = wrap(target.osType(), command, workdir);
            try (Session session = ssh.startSession()) {
                Session.Command cmd = session.exec(wrapped);
                cmd.join(timeoutSeconds, TimeUnit.SECONDS);
                Integer exit = cmd.getExitStatus();
                String out = read(cmd.getInputStream());
                String err = read(cmd.getErrorStream());
                if (exit == null) {
                    return new ExecResult(-1, out, "执行超时或被中断", true);
                }
                return new ExecResult(exit, out, err, false);
            }
        } catch (Exception e) {
            log.warn("SSH 执行失败 host={}: {}", target.host(), e.getMessage());
            return new ExecResult(-1, "", rootMessage(e), false);
        } finally {
            try {
                ssh.disconnect();
            } catch (Exception ignored) {
                // ignore
            }
        }
    }

    private String wrap(String osType, String command, String workdir) {
        if (workdir == null || workdir.isBlank()) {
            return command;
        }
        if ("Windows".equalsIgnoreCase(osType)) {
            return "cd /d \"" + workdir + "\" && " + command;
        }
        return "cd \"" + workdir + "\" && " + command;
    }

    private String read(InputStream in) {
        try {
            String text = IOUtils.readFully(in).toString();
            if (text.length() > maxOutputBytes) {
                return text.substring(0, maxOutputBytes) + "\n...[输出截断]";
            }
            return text;
        } catch (Exception e) {
            return "";
        }
    }

    private static String rootMessage(Throwable error) {
        Throwable cause = error;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        String message = cause.getMessage();
        return message == null ? cause.getClass().getSimpleName() : message;
    }
}
