package cn.sichu.system.eda.controller;

import cn.sichu.common.exception.BusinessException;
import cn.sichu.common.result.Result;
import cn.sichu.common.result.ResultCode;
import cn.sichu.system.eda.entity.EdaServer;
import cn.sichu.system.eda.entity.EdaTool;
import cn.sichu.system.eda.executor.RemoteExecutor;
import cn.sichu.system.eda.mapper.EdaServerMapper;
import cn.sichu.system.eda.mapper.EdaToolMapper;
import cn.sichu.system.eda.model.ServerSaveForm;
import cn.sichu.system.eda.model.ServerVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 服务器管理：SSH 目标元数据。认证凭据由部署配置提供，页面与接口不接触私钥。
 *
 * @author sichu huang
 * @since 2026/11/15
 */
@RestController
@RequestMapping("/api/eda/servers")
@RequiredArgsConstructor
public class EdaServerController {

    private final EdaServerMapper serverMapper;

    private final EdaToolMapper toolMapper;

    private final RemoteExecutor remoteExecutor;

    @GetMapping
    public Result<List<ServerVO>> list(@RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<EdaServer> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(EdaServer::getServerCode, keyword)
                .or().like(EdaServer::getServerName, keyword).or().like(EdaServer::getHost, keyword));
        }
        wrapper.orderByAsc(EdaServer::getId);
        return Result.success(serverMapper.selectList(wrapper).stream().map(this::toVO).toList());
    }

    @PostMapping
    public Result<String> create(@RequestBody ServerSaveForm form) {
        validate(form, true);
        EdaServer server = new EdaServer();
        apply(server, form, true);
        serverMapper.insert(server);
        return Result.success(server.getId().toString());
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody ServerSaveForm form) {
        EdaServer server = serverMapper.selectById(id);
        if (server == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "服务器不存在");
        }
        if (StringUtils.hasText(form.getServerCode())) {
            Long dup = serverMapper.selectCount(new LambdaQueryWrapper<EdaServer>()
                .eq(EdaServer::getServerCode, form.getServerCode().trim())
                .ne(EdaServer::getId, id));
            if (dup != null && dup > 0) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "服务器编码已存在");
            }
        }
        apply(server, form, false);
        serverMapper.updateById(server);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        EdaServer server = serverMapper.selectById(id);
        if (server == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "服务器不存在");
        }
        Long refs = toolMapper.selectCount(
            new LambdaQueryWrapper<EdaTool>().eq(EdaTool::getServerId, id));
        if (refs != null && refs > 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "该服务器下还有 EDA 工具，不能删除");
        }
        serverMapper.deleteById(id);
        return Result.success();
    }

    @PostMapping("/{id}/test")
    public Result<RemoteExecutor.ExecResult> test(@PathVariable Long id) {
        EdaServer server = serverMapper.selectById(id);
        if (server == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "服务器不存在");
        }
        RemoteExecutor.Target target = new RemoteExecutor.Target(server.getHost(),
            server.getSshPort() == null ? 22 : server.getSshPort(), server.getUsername(),
            server.getOsType() == null ? "Linux" : server.getOsType(),
            server.getConnectTimeoutSeconds() == null ? 10 : server.getConnectTimeoutSeconds());
        return Result.success(remoteExecutor.exec(target, "echo ems-ok", null, 15));
    }

    private void validate(ServerSaveForm form, boolean creating) {
        if (creating) {
            if (!StringUtils.hasText(form.getServerCode())) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "服务器编码不能为空");
            }
            if (!StringUtils.hasText(form.getHost())) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "主机不能为空");
            }
            Long dup = serverMapper.selectCount(new LambdaQueryWrapper<EdaServer>()
                .eq(EdaServer::getServerCode, form.getServerCode().trim()));
            if (dup != null && dup > 0) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "服务器编码已存在");
            }
        }
    }

    private void apply(EdaServer server, ServerSaveForm form, boolean creating) {
        if (StringUtils.hasText(form.getServerCode())) {
            server.setServerCode(form.getServerCode().trim());
        }
        if (form.getServerName() != null) {
            server.setServerName(form.getServerName());
        }
        if (StringUtils.hasText(form.getHost())) {
            server.setHost(form.getHost().trim());
        }
        if (form.getSshPort() != null) {
            server.setSshPort(form.getSshPort());
        }
        if (StringUtils.hasText(form.getOsType())) {
            server.setOsType(form.getOsType());
        }
        if (form.getUsername() != null) {
            server.setUsername(form.getUsername());
        }
        if (form.getConnectTimeoutSeconds() != null) {
            server.setConnectTimeoutSeconds(form.getConnectTimeoutSeconds());
        }
        if (form.getStatus() != null) {
            server.setStatus(form.getStatus());
        }
        if (form.getRemark() != null) {
            server.setRemark(form.getRemark());
        }
        if (server.getSshPort() == null) {
            server.setSshPort(22);
        }
        if (server.getOsType() == null) {
            server.setOsType("Linux");
        }
        if (server.getConnectTimeoutSeconds() == null) {
            server.setConnectTimeoutSeconds(10);
        }
        if (server.getStatus() == null) {
            server.setStatus(1);
        }
    }

    private ServerVO toVO(EdaServer server) {
        ServerVO vo = new ServerVO();
        vo.setId(server.getId());
        vo.setServerCode(server.getServerCode());
        vo.setServerName(server.getServerName());
        vo.setHost(server.getHost());
        vo.setSshPort(server.getSshPort());
        vo.setOsType(server.getOsType());
        vo.setUsername(server.getUsername());
        vo.setConnectTimeoutSeconds(server.getConnectTimeoutSeconds());
        vo.setStatus(server.getStatus());
        vo.setRemark(server.getRemark());
        vo.setCreateTime(server.getCreateTime());
        vo.setUpdateTime(server.getUpdateTime());
        return vo;
    }
}
