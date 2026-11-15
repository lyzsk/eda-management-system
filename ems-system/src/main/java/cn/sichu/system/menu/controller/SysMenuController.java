package cn.sichu.system.menu.controller;

import cn.sichu.common.exception.BusinessException;
import cn.sichu.common.result.Result;
import cn.sichu.common.result.ResultCode;
import cn.sichu.system.menu.entity.SysMenu;
import cn.sichu.system.menu.mapper.SysMenuMapper;
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
@RequestMapping("/api/system/menus")
@RequiredArgsConstructor
public class SysMenuController {

    private final SysMenuMapper menuMapper;

    @GetMapping
    public Result<List<SysMenu>> list(@RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysMenu::getMenuName, keyword).or()
                .like(SysMenu::getMenuCode, keyword));
        }
        wrapper.orderByAsc(SysMenu::getSortNo).orderByAsc(SysMenu::getId);
        return Result.success(buildTree(menuMapper.selectList(wrapper)));
    }

    @PostMapping
    public Result<String> create(@RequestBody SysMenu menu) {
        if (!StringUtils.hasText(menu.getMenuCode())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "菜单编码不能为空");
        }
        Long count = menuMapper.selectCount(
            new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getMenuCode, menu.getMenuCode()));
        if (count != null && count > 0) {
            throw new BusinessException("菜单编码已存在");
        }
        menu.setId(null);
        if (menu.getParentId() == null) {
            menu.setParentId(0L);
        }
        if (menu.getStatus() == null) {
            menu.setStatus(1);
        }
        if (menu.getSortNo() == null) {
            menu.setSortNo(0);
        }
        if (!StringUtils.hasText(menu.getMenuType())) {
            menu.setMenuType("MENU");
        }
        menuMapper.insert(menu);
        return Result.success(menu.getId().toString());
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysMenu menu) {
        SysMenu existing = menuMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "菜单不存在");
        }
        if (menu.getParentId() != null && menu.getParentId().equals(id)) {
            throw new BusinessException("上级菜单不能是自己");
        }
        menu.setId(id);
        menuMapper.updateById(menu);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        SysMenu existing = menuMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "菜单不存在");
        }
        Long childCount =
            menuMapper.selectCount(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, id));
        if (childCount != null && childCount > 0) {
            throw new BusinessException("请先删除子菜单");
        }
        menuMapper.deleteById(id);
        return Result.success();
    }

    private List<SysMenu> buildTree(List<SysMenu> menus) {
        Map<Long, SysMenu> byId =
            menus.stream().collect(Collectors.toMap(SysMenu::getId, Function.identity()));
        List<SysMenu> roots = new ArrayList<>();
        for (SysMenu menu : menus) {
            menu.setChildren(new ArrayList<>());
            Long parentId = menu.getParentId();
            SysMenu parent = parentId == null ? null : byId.get(parentId);
            if (parentId == null || parentId == 0 || parent == null) {
                roots.add(menu);
            } else {
                parent.getChildren().add(menu);
            }
        }
        return roots;
    }
}
