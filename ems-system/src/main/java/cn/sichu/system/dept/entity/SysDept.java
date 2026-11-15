package cn.sichu.system.dept.entity;

import cn.sichu.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author sichu huang
 * @since 2026/11/15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dept")
public class SysDept extends BaseEntity {

    private Long parentId;

    private String deptCode;

    private String deptName;

    private Integer sortNo;

    private Integer status;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private List<SysDept> children = new ArrayList<>();
}
