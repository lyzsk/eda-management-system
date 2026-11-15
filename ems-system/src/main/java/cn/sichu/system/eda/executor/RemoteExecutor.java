package cn.sichu.system.eda.executor;

/**
 * 远程执行器抽象：SSH 先实现，后续可加 WinRM / Agent 实现而不改上层。
 *
 * @author sichu huang
 * @since 2026/11/15
 */
public interface RemoteExecutor {

    ExecResult exec(Target target, String command, String workdir, int timeoutSeconds);

    /**
     * 连接参数；不含任何密钥，私钥由执行器从部署配置读取。
     */
    record Target(String host, int port, String username, String osType,
                  int connectTimeoutSeconds) {
    }

    /**
     * 执行结果
     */
    record ExecResult(int exitCode, String output, String errorMsg, boolean timedOut) {

        public boolean success() {
            return !timedOut && exitCode == 0;
        }
    }
}
