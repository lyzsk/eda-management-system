package cn.sichu.system.user.controller;

import cn.sichu.common.exception.BusinessException;
import cn.sichu.common.result.Result;
import cn.sichu.common.result.ResultCode;
import cn.sichu.system.dept.entity.SysDept;
import cn.sichu.system.dept.mapper.SysDeptMapper;
import cn.sichu.system.user.entity.SysUser;
import cn.sichu.system.user.mapper.SysUserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author sichu huang
 * @since 2026/11/15
 */
@RestController
@RequestMapping("/api/system/users")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserMapper userMapper;
    private final SysDeptMapper deptMapper;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public Result<List<SysUser>> list(@RequestParam(required = false) String keyword,
        @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysUser::getUsername, keyword).or()
                .like(SysUser::getRealName, keyword));
        }
        if (status != null) {
            wrapper.eq(SysUser::getStatus, status);
        }
        wrapper.orderByAsc(SysUser::getId);
        List<SysUser> users = userMapper.selectList(wrapper);
        Map<Long, String> deptNames = deptMapper.selectList(null).stream()
            .collect(Collectors.toMap(SysDept::getId, SysDept::getDeptName));
        users.forEach(user -> user.setDeptName(deptNames.get(user.getDeptId())));
        return Result.success(users);
    }

    @PostMapping
    public Result<String> create(@RequestBody SysUser user) {
        if (!StringUtils.hasText(user.getUsername())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "用户名不能为空");
        }
        Long count = userMapper.selectCount(
            new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, user.getUsername()));
        if (count != null && count > 0) {
            throw new BusinessException("用户名已存在");
        }
        user.setId(null);
        user.setPassword(passwordEncoder.encode(
            StringUtils.hasText(user.getPassword()) ? user.getPassword() : "admin123"));
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        if (user.getGender() == null) {
            user.setGender(0);
        }
        if (!StringUtils.hasText(user.getRoleCode())) {
            user.setRoleCode("USER");
        }
        userMapper.insert(user);
        return Result.success(user.getId().toString());
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysUser user) {
        SysUser existing = userMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        if (StringUtils.hasText(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(null);
        }
        user.setId(id);
        userMapper.updateById(user);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        SysUser existing = userMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        if ("admin".equals(existing.getUsername())) {
            throw new BusinessException("内置管理员不能删除");
        }
        userMapper.deleteById(id);
        return Result.success();
    }
}
