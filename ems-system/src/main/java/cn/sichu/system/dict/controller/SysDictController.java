package cn.sichu.system.dict.controller;

import cn.sichu.common.exception.BusinessException;
import cn.sichu.common.result.Result;
import cn.sichu.common.result.ResultCode;
import cn.sichu.system.dict.entity.SysDictData;
import cn.sichu.system.dict.entity.SysDictType;
import cn.sichu.system.dict.mapper.SysDictDataMapper;
import cn.sichu.system.dict.mapper.SysDictTypeMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *
 * @author sichu huang
 * @since 2026/11/15
 */
@RestController
@RequestMapping("/api/system/dict")
@RequiredArgsConstructor
public class SysDictController {

    private final SysDictTypeMapper typeMapper;
    private final SysDictDataMapper dataMapper;

    @GetMapping("/types")
    public Result<List<SysDictType>> listTypes(@RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysDictType::getDictName, keyword).or()
                .like(SysDictType::getDictType, keyword));
        }
        wrapper.orderByAsc(SysDictType::getId);
        return Result.success(typeMapper.selectList(wrapper));
    }

    @PostMapping("/types")
    public Result<String> createType(@RequestBody SysDictType type) {
        if (!StringUtils.hasText(type.getDictType())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "字典类型不能为空");
        }
        Long count = typeMapper.selectCount(
            new LambdaQueryWrapper<SysDictType>().eq(SysDictType::getDictType, type.getDictType()));
        if (count != null && count > 0) {
            throw new BusinessException("字典类型已存在");
        }
        type.setId(null);
        if (type.getStatus() == null) {
            type.setStatus(1);
        }
        typeMapper.insert(type);
        return Result.success(type.getId().toString());
    }

    @PutMapping("/types/{id}")
    public Result<Void> updateType(@PathVariable Long id, @RequestBody SysDictType type) {
        SysDictType existing = typeMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "字典类型不存在");
        }
        type.setId(id);
        typeMapper.updateById(type);
        return Result.success();
    }

    @DeleteMapping("/types/{id}")
    @Transactional
    public Result<Void> deleteType(@PathVariable Long id) {
        SysDictType existing = typeMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "字典类型不存在");
        }
        dataMapper.delete(new LambdaQueryWrapper<SysDictData>().eq(SysDictData::getDictType,
            existing.getDictType()));
        typeMapper.deleteById(id);
        return Result.success();
    }

    @GetMapping("/data")
    public Result<List<SysDictData>> listData(@RequestParam(required = false) String dictType) {
        LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dictType)) {
            wrapper.eq(SysDictData::getDictType, dictType);
        }
        wrapper.orderByAsc(SysDictData::getDictType).orderByAsc(SysDictData::getSortNo)
            .orderByAsc(SysDictData::getId);
        return Result.success(dataMapper.selectList(wrapper));
    }

    @PostMapping("/data")
    public Result<String> createData(@RequestBody SysDictData data) {
        if (!StringUtils.hasText(data.getDictType())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "字典类型不能为空");
        }
        if (!StringUtils.hasText(data.getDictValue())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "字典值不能为空");
        }
        Long count = dataMapper.selectCount(
            new LambdaQueryWrapper<SysDictData>().eq(SysDictData::getDictType, data.getDictType())
                .eq(SysDictData::getDictValue, data.getDictValue()));
        if (count != null && count > 0) {
            throw new BusinessException("该字典类型下字典值已存在");
        }
        data.setId(null);
        if (data.getStatus() == null) {
            data.setStatus(1);
        }
        if (data.getSortNo() == null) {
            data.setSortNo(0);
        }
        dataMapper.insert(data);
        return Result.success(data.getId().toString());
    }

    @PutMapping("/data/{id}")
    public Result<Void> updateData(@PathVariable Long id, @RequestBody SysDictData data) {
        SysDictData existing = dataMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "字典数据不存在");
        }
        data.setId(id);
        dataMapper.updateById(data);
        return Result.success();
    }

    @DeleteMapping("/data/{id}")
    public Result<Void> deleteData(@PathVariable Long id) {
        SysDictData existing = dataMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "字典数据不存在");
        }
        dataMapper.deleteById(id);
        return Result.success();
    }
}
