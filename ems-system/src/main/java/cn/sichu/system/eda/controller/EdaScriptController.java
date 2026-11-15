package cn.sichu.system.eda.controller;

import cn.sichu.common.exception.BusinessException;
import cn.sichu.common.result.Result;
import cn.sichu.common.result.ResultCode;
import cn.sichu.system.eda.entity.EdaScript;
import cn.sichu.system.eda.entity.EdaScriptRun;
import cn.sichu.system.eda.mapper.EdaScriptMapper;
import cn.sichu.system.eda.mapper.EdaScriptRunMapper;
import cn.sichu.system.eda.service.ScriptExecService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 脚本管理：登记 EDA 的启动/停止/重启/状态/任务脚本，异步执行并保留执行历史。
 *
 * @author sichu huang
 * @since 2026/11/15
 */
@RestController
@RequestMapping("/api/eda/scripts")
@RequiredArgsConstructor
public class EdaScriptController {

    private static final Set<String> ACTIONS = Set.of("START", "STOP", "RESTART", "STATUS",
        "TASK");

    private static final Set<String> EXEC_MODES = Set.of("SHELL", "SCRIPT", "POWERSHELL");

    private final EdaScriptMapper scriptMapper;

    private final EdaScriptRunMapper runMapper;

    private final ScriptExecService execService;

    @GetMapping
    public Result<List<EdaScript>> list(@RequestParam(required = false) Long toolId) {
        LambdaQueryWrapper<EdaScript> wrapper = new LambdaQueryWrapper<>();
        if (toolId != null) {
            wrapper.eq(EdaScript::getToolId, toolId);
        }
        wrapper.orderByAsc(EdaScript::getToolId).orderByAsc(EdaScript::getSortNo)
            .orderByAsc(EdaScript::getId);
        return Result.success(scriptMapper.selectList(wrapper));
    }

    @PostMapping
    public Result<String> create(@RequestBody EdaScript script) {
        validate(script);
        script.setId(null);
        if (script.getEnabled() == null) {
            script.setEnabled(1);
        }
        if (script.getSortNo() == null) {
            script.setSortNo(0);
        }
        if (script.getTimeoutSeconds() == null) {
            script.setTimeoutSeconds(60);
        }
        if (!StringUtils.hasText(script.getExecMode())) {
            script.setExecMode("SHELL");
        }
        scriptMapper.insert(script);
        return Result.success(script.getId().toString());
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody EdaScript script) {
        EdaScript existing = scriptMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "脚本不存在");
        }
        if (script.getAction() != null || script.getCommand() != null
            || script.getToolId() != null) {
            validate(script);
        }
        script.setId(id);
        scriptMapper.updateById(script);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        EdaScript existing = scriptMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "脚本不存在");
        }
        scriptMapper.deleteById(id);
        return Result.success();
    }

    @PostMapping("/{id}/run")
    public Result<String> run(@PathVariable Long id) {
        return Result.success(execService.start(id, currentOperator()));
    }

    @GetMapping("/runs/{runId}")
    public Result<EdaScriptRun> getRun(@PathVariable String runId) {
        EdaScriptRun run = runMapper.selectOne(
            new LambdaQueryWrapper<EdaScriptRun>().eq(EdaScriptRun::getRunId, runId));
        if (run == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "执行记录不存在");
        }
        return Result.success(run);
    }

    @GetMapping("/runs")
    public Result<List<EdaScriptRun>> listRuns(@RequestParam(required = false) Long toolId,
        @RequestParam(defaultValue = "50") Integer limit) {
        int size = limit == null ? 50 : Math.min(Math.max(limit, 1), 200);
        LambdaQueryWrapper<EdaScriptRun> wrapper = new LambdaQueryWrapper<>();
        if (toolId != null) {
            wrapper.eq(EdaScriptRun::getToolId, toolId);
        }
        // 历史列表不带完整输出，避免大 payload；查看详情走 /runs/{runId}
        wrapper.select(EdaScriptRun.class, info -> !"output".equals(info.getColumn()))
            .orderByDesc(EdaScriptRun::getId).last("limit " + size);
        return Result.success(runMapper.selectList(wrapper));
    }

    private void validate(EdaScript script) {
        if (script.getToolId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "必须关联 EDA 工具");
        }
        if (!StringUtils.hasText(script.getScriptName())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "脚本名称不能为空");
        }
        if (!StringUtils.hasText(script.getCommand())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "命令不能为空");
        }
        if (script.getAction() != null && !ACTIONS.contains(script.getAction())) {
            throw new BusinessException(ResultCode.PARAM_ERROR,
                "动作必须是 START/STOP/RESTART/STATUS/TASK");
        }
        if (script.getExecMode() != null && !EXEC_MODES.contains(script.getExecMode())) {
            throw new BusinessException(ResultCode.PARAM_ERROR,
                "执行方式必须是 SHELL/SCRIPT/POWERSHELL");
        }
        if (script.getAction() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "动作不能为空");
        }
    }

    private String currentOperator() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null || authentication.getName() == null ? "unknown"
            : authentication.getName();
    }
}
