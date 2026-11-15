package cn.sichu.system.eda.entity;

import cn.sichu.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 *
 * @author sichu huang
 * @since 2026/11/15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("eda_tool")
public class EdaTool extends BaseEntity {

    private String toolName;

    private String toolType;

    private String host;

    private String osType;

    private String toolVersion;

    private String licenseStatus;

    private Integer status;

    private LocalDateTime lastHeartbeat;

    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Long serverId;
}
