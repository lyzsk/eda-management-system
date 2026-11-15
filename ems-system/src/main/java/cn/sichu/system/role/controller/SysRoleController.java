package cn.sichu.system.role.controller;

import cn.sichu.common.exception.BusinessException;
import cn.sichu.common.result.Result;
import cn.sichu.common.result.ResultCode;
import cn.sichu.system.role.entity.SysRole;
import cn.sichu.system.role.mapper.SysRoleMapper;
import cn.sichu.system.user.entity.SysUser;
import cn.sichu.system.user.mapper.SysUserMapper;
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
@RequestMapping("/api/system/roles")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleMapper roleMapper;
    private final SysUserMapper userMapper;

    @GetMapping
    public Result<List<SysRole>> list(@RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysRole::getRoleName, keyword).or()
                .like(SysRole::getRoleCode, keyword));
        }
        wrapper.orderByAsc(SysRole::getSortNo).orderByAsc(SysRole::getId);
        return Result.success(roleMapper.selectList(wrapper));
    }

    @PostMapping
    public Result<String> create(@RequestBody SysRole role) {
        if (!StringUtils.hasText(role.getRoleCode())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "角色编码不能为空");
        }
        Long count = roleMapper.selectCount(
            new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, role.getRoleCode()));
        if (count != null && count > 0) {
            throw new BusinessException("角色编码已存在");
        }
        role.setId(null);
        if (role.getStatus() == null) {
            role.setStatus(1);
        }
        if (role.getSortNo() == null) {
            role.setSortNo(0);
        }
        roleMapper.insert(role);
        return Result.success(role.getId().toString());
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysRole role) {
        SysRole existing = roleMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "角色不存在");
        }
        if ("ADMIN".equals(existing.getRoleCode()) && StringUtils.hasText(role.getRoleCode())
            && !"ADMIN".equals(role.getRoleCode())) {
            throw new BusinessException("内置管理员角色编码不能修改");
        }
        role.setId(id);
        roleMapper.updateById(role);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        SysRole existing = roleMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "角色不存在");
        }
        if ("ADMIN".equals(existing.getRoleCode())) {
            throw new BusinessException("内置管理员角色不能删除");
        }
        Long userCount = userMapper.selectCount(
            new LambdaQueryWrapper<SysUser>().eq(SysUser::getRoleCode, existing.getRoleCode()));
        if (userCount != null && userCount > 0) {
            throw new BusinessException("该角色下还有用户，不能删除");
        }
        roleMapper.deleteById(id);
        return Result.success();
    }
}
