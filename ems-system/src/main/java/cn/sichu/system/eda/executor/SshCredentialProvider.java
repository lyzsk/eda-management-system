package cn.sichu.system.eda.executor;

import cn.sichu.common.exception.BusinessException;
import cn.sichu.common.result.ResultCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * SSH 凭据提供者（方案 A）：私钥与默认账号来自部署配置，不进数据库、不经业务层。
 *
 * @author sichu huang
 * @since 2026/11/15
 */
@Component
public class SshCredentialProvider {

    private final String privateKey;

    private final String passphrase;

    private final String defaultUsername;

    public SshCredentialProvider(@Value("${ems.remote.ssh.private-key:}") String inlineKey,
        @Value("${ems.remote.ssh.private-key-file:}") String keyFile,
        @Value("${ems.remote.ssh.private-key-passphrase:}") String passphrase,
        @Value("${ems.remote.ssh.default-username:}") String defaultUsername) {
        this.privateKey = resolveKey(inlineKey, keyFile);
        this.passphrase = passphrase;
        this.defaultUsername = defaultUsername;
    }

    public boolean configured() {
        return StringUtils.hasText(privateKey);
    }

    public String requirePrivateKey() {
        if (!configured()) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR,
                "未配置 SSH 私钥：请设置 EMS_SSH_KEY_FILE（私钥文件路径）或 EMS_SSH_PRIVATE_KEY（私钥内容）");
        }
        return privateKey;
    }

    public String passphrase() {
        return StringUtils.hasText(passphrase) ? passphrase : null;
    }

    /**
     * 服务器单独配置的账号优先，否则用全局默认账号。
     */
    public String username(String serverUsername) {
        if (StringUtils.hasText(serverUsername)) {
            return serverUsername;
        }
        if (StringUtils.hasText(defaultUsername)) {
            return defaultUsername;
        }
        throw new BusinessException(ResultCode.BUSINESS_ERROR,
            "未配置 SSH 执行账号：请在服务器上填写账号或设置 EMS_SSH_USER");
    }

    private static String resolveKey(String inlineKey, String keyFile) {
        if (StringUtils.hasText(inlineKey)) {
            return inlineKey;
        }
        if (StringUtils.hasText(keyFile)) {
            try {
                return Files.readString(Path.of(keyFile), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new IllegalStateException("读取 SSH 私钥文件失败: " + keyFile, e);
            }
        }
        return null;
    }
}
