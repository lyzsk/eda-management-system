package cn.sichu.system.eda.entity;

import cn.sichu.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 脚本执行历史
 *
 * @author sichu huang
 * @since 2026/11/15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("eda_script_run")
public class EdaScriptRun extends BaseEntity {

    private String runId;

    private Long scriptId;

    private Long toolId;

    private Long serverId;

    private String action;

    private String operatorName;

    private String status;

    private Integer exitCode;

    private String output;

    private String errorMsg;

    private String sshHost;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;
}
