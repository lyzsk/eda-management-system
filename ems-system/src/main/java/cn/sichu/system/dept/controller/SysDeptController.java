package cn.sichu.system.dept.controller;

import cn.sichu.common.exception.BusinessException;
import cn.sichu.common.result.Result;
import cn.sichu.common.result.ResultCode;
import cn.sichu.system.dept.entity.SysDept;
import cn.sichu.system.dept.mapper.SysDeptMapper;
import cn.sichu.system.user.entity.SysUser;
import cn.sichu.system.user.mapper.SysUserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @author sichu huang
 * @since 2026/11/15
 */
@RestController
@RequestMapping("/api/system/depts")
@RequiredArgsConstructor
public class SysDeptController {

    private final SysDeptMapper deptMapper;
    private final SysUserMapper userMapper;

    @GetMapping
    public Result<List<SysDept>> list(@RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysDept::getDeptName, keyword).or()
                .like(SysDept::getDeptCode, keyword));
        }
        wrapper.orderByAsc(SysDept::getSortNo).orderByAsc(SysDept::getId);
        return Result.success(buildTree(deptMapper.selectList(wrapper)));
    }

    @PostMapping
    public Result<String> create(@RequestBody SysDept dept) {
        if (!StringUtils.hasText(dept.getDeptCode())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "部门编码不能为空");
        }
        if (!StringUtils.hasText(dept.getDeptName())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "部门名称不能为空");
        }
        Long count = deptMapper.selectCount(
            new LambdaQueryWrapper<SysDept>().eq(SysDept::getDeptCode, dept.getDeptCode()));
        if (count != null && count > 0) {
            throw new BusinessException("部门编码已存在");
        }
        dept.setId(null);
        if (dept.getParentId() == null) {
            dept.setParentId(0L);
        }
        if (dept.getStatus() == null) {
            dept.setStatus(1);
        }
        if (dept.getSortNo() == null) {
            dept.setSortNo(0);
        }
        deptMapper.insert(dept);
        return Result.success(dept.getId().toString());
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysDept dept) {
        SysDept existing = deptMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "部门不存在");
        }
        if (id != null && id == 0L) {
            dept.setParentId(0L);
        } else if (dept.getParentId() != null && dept.getParentId().equals(id)) {
            throw new BusinessException("上级部门不能是自己");
        }
        dept.setId(id);
        deptMapper.updateById(dept);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        SysDept existing = deptMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "部门不存在");
        }
        if (id != null && id == 0L) {
            throw new BusinessException("公司根节点不能删除");
        }
        Long childCount =
            deptMapper.selectCount(new LambdaQueryWrapper<SysDept>().eq(SysDept::getParentId, id));
        if (childCount != null && childCount > 0) {
            throw new BusinessException("请先删除子部门");
        }
        Long userCount =
            userMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getDeptId, id));
        if (userCount != null && userCount > 0) {
            throw new BusinessException("部门下还有用户，不能删除");
        }
        deptMapper.deleteById(id);
        return Result.success();
    }

    private List<SysDept> buildTree(List<SysDept> depts) {
        Map<Long, SysDept> byId =
            depts.stream().collect(Collectors.toMap(SysDept::getId, Function.identity()));
        List<SysDept> roots = new ArrayList<>();
        for (SysDept dept : depts) {
            dept.setChildren(new ArrayList<>());
            Long parentId = dept.getParentId();
            SysDept parent = parentId == null ? null : byId.get(parentId);
            if ((dept.getId() != null && dept.getId() == 0L)
                || parentId == null || parent == null) {
                roots.add(dept);
            } else {
                parent.getChildren().add(dept);
            }
        }
        return roots;
    }
}
