package cn.sichu.system.eda.entity;

import cn.sichu.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * EDA 服务操作脚本（启动/停止/重启/状态/任务）
 *
 * @author sichu huang
 * @since 2026/11/15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("eda_script")
public class EdaScript extends BaseEntity {

    private Long toolId;

    private String action;

    private String scriptName;

    private String execMode;

    private String command;

    private String workdir;

    private Integer timeoutSeconds;

    private Integer enabled;

    private Integer sortNo;
}
