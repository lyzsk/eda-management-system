package cn.sichu.system.eda.controller;

import cn.sichu.common.exception.BusinessException;
import cn.sichu.common.result.Result;
import cn.sichu.common.result.ResultCode;
import cn.sichu.system.eda.entity.EdaTool;
import cn.sichu.system.eda.mapper.EdaToolMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *
 * @author sichu huang
 * @since 2026/11/15
 */
@RestController
@RequestMapping("/api/eda/tools")
@RequiredArgsConstructor
public class EdaToolController {

    private final EdaToolMapper toolMapper;

    @GetMapping
    public Result<List<EdaTool>> list(@RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<EdaTool> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(
                w -> w.like(EdaTool::getToolName, keyword).or().like(EdaTool::getHost, keyword));
        }
        wrapper.orderByAsc(EdaTool::getId);
        return Result.success(toolMapper.selectList(wrapper));
    }

    @PostMapping
    public Result<String> create(@RequestBody EdaTool tool) {
        if (!StringUtils.hasText(tool.getToolName())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "工具名称不能为空");
        }
        tool.setId(null);
        if (tool.getStatus() == null) {
            tool.setStatus(1);
        }
        if (!StringUtils.hasText(tool.getLicenseStatus())) {
            tool.setLicenseStatus("UNKNOWN");
        }
        toolMapper.insert(tool);
        return Result.success(tool.getId().toString());
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody EdaTool tool) {
        EdaTool existing = toolMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "EDA 工具不存在");
        }
        tool.setId(id);
        toolMapper.updateById(tool);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        EdaTool existing = toolMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "EDA 工具不存在");
        }
        toolMapper.deleteById(id);
        return Result.success();
    }
}
