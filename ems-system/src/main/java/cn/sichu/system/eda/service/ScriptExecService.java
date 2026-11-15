package cn.sichu.system.eda.service;

import cn.sichu.common.exception.BusinessException;
import cn.sichu.common.result.ResultCode;
import cn.sichu.system.eda.entity.EdaScript;
import cn.sichu.system.eda.entity.EdaScriptRun;
import cn.sichu.system.eda.entity.EdaServer;
import cn.sichu.system.eda.entity.EdaTool;
import cn.sichu.system.eda.executor.RemoteExecutor;
import cn.sichu.system.eda.mapper.EdaScriptMapper;
import cn.sichu.system.eda.mapper.EdaScriptRunMapper;
import cn.sichu.system.eda.mapper.EdaServerMapper;
import cn.sichu.system.eda.mapper.EdaToolMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 脚本执行服务：创建执行记录 → 线程池异步 SSH 执行 → 回写结果。
 *
 * @author sichu huang
 * @since 2026/11/15
 */
@Slf4j
@Service
public class ScriptExecService {

    private static final int MAX_OUTPUT_LENGTH = 65536;

    private final EdaScriptMapper scriptMapper;

    private final EdaScriptRunMapper runMapper;

    private final EdaToolMapper toolMapper;

    private final EdaServerMapper serverMapper;

    private final RemoteExecutor remoteExecutor;

    private final ThreadPoolTaskExecutor executor;

    public ScriptExecService(EdaScriptMapper scriptMapper, EdaScriptRunMapper runMapper,
        EdaToolMapper toolMapper, EdaServerMapper serverMapper, RemoteExecutor remoteExecutor,
        @Qualifier("scriptRunExecutor") ThreadPoolTaskExecutor executor) {
        this.scriptMapper = scriptMapper;
        this.runMapper = runMapper;
        this.toolMapper = toolMapper;
        this.serverMapper = serverMapper;
        this.remoteExecutor = remoteExecutor;
        this.executor = executor;
    }

    /**
     * 创建执行记录并提交异步任务，返回 runId 供前端轮询。
     */
    public String start(Long scriptId, String operator) {
        EdaScript script = requireScript(scriptId);
        if (script.getEnabled() != null && script.getEnabled() == 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "脚本已停用");
        }
        EdaTool tool = toolMapper.selectById(script.getToolId());
        if (tool == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "关联的 EDA 工具不存在");
        }
        if (tool.getServerId() == null) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "该 EDA 工具尚未关联服务器");
        }
        EdaServer server = serverMapper.selectById(tool.getServerId());
        if (server == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "关联的服务器不存在");
        }
        if (server.getStatus() != null && server.getStatus() == 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "服务器已停用");
        }

        EdaScriptRun run = new EdaScriptRun();
        run.setRunId(UUID.randomUUID().toString().replace("-", ""));
        run.setScriptId(script.getId());
        run.setToolId(tool.getId());
        run.setServerId(server.getId());
        run.setAction(script.getAction());
        run.setOperatorName(operator);
        run.setStatus("PENDING");
        run.setSshHost(server.getHost());
        runMapper.insert(run);

        String runId = run.getRunId();
        executor.execute(() -> execute(runId));
        return runId;
    }

    /**
     * 异步执行体；所有异常都落库，不再向外抛。
     */
    public void execute(String runId) {
        EdaScriptRun run = runMapper.selectOne(
            new LambdaQueryWrapper<EdaScriptRun>().eq(EdaScriptRun::getRunId, runId));
        if (run == null) {
            log.warn("脚本执行记录不存在 runId={}", runId);
            return;
        }
        run.setStatus("RUNNING");
        run.setStartedAt(LocalDateTime.now());
        runMapper.updateById(run);
        try {
            EdaScript script = requireScript(run.getScriptId());
            EdaServer server = serverMapper.selectById(run.getServerId());
            if (server == null) {
                throw new BusinessException(ResultCode.NOT_FOUND, "服务器不存在");
            }
            int timeout = script.getTimeoutSeconds() == null ? 60
                : Math.max(1, script.getTimeoutSeconds());
            RemoteExecutor.Target target = new RemoteExecutor.Target(server.getHost(),
                server.getSshPort() == null ? 22 : server.getSshPort(), server.getUsername(),
                server.getOsType() == null ? "Linux" : server.getOsType(),
                server.getConnectTimeoutSeconds() == null ? 10
                    : server.getConnectTimeoutSeconds());
            RemoteExecutor.ExecResult result = remoteExecutor.exec(target, buildCommand(script),
                script.getWorkdir(), timeout);
            run.setExitCode(result.exitCode());
            run.setOutput(joinOutput(result));
            run.setErrorMsg(result.errorMsg());
            run.setStatus(
                result.timedOut() ? "TIMEOUT" : result.exitCode() == 0 ? "SUCCESS" : "FAILED");
        } catch (Exception e) {
            log.warn("脚本执行失败 runId={}: {}", runId, e.getMessage());
            run.setStatus("FAILED");
            run.setErrorMsg(e.getMessage());
        } finally {
            run.setFinishedAt(LocalDateTime.now());
            runMapper.updateById(run);
        }
    }

    private EdaScript requireScript(Long scriptId) {
        EdaScript script = scriptMapper.selectById(scriptId);
        if (script == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "脚本不存在");
        }
        return script;
    }

    private String buildCommand(EdaScript script) {
        if (!StringUtils.hasText(script.getCommand())) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "脚本命令为空");
        }
        String mode = script.getExecMode() == null ? "SHELL" : script.getExecMode();
        if ("POWERSHELL".equalsIgnoreCase(mode)) {
            return "powershell -NoProfile -NonInteractive -Command \"" + script.getCommand()
                + "\"";
        }
        return script.getCommand();
    }

    private String joinOutput(RemoteExecutor.ExecResult result) {
        StringBuilder text = new StringBuilder();
        if (StringUtils.hasText(result.output())) {
            text.append(result.output());
        }
        if (StringUtils.hasText(result.errorMsg())) {
            if (!text.isEmpty()) {
                text.append('\n');
            }
            text.append("[stderr] ").append(result.errorMsg());
        }
        String output = text.toString();
        if (output.length() > MAX_OUTPUT_LENGTH) {
            return output.substring(0, MAX_OUTPUT_LENGTH) + "\n...[输出截断]";
        }
        return output;
    }
}
