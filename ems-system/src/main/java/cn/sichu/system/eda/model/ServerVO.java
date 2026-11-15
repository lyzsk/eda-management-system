package cn.sichu.system.eda.model;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 服务器返回对象：不包含任何凭据字段。
 *
 * @author sichu huang
 * @since 2026/11/15
 */
@Data
public class ServerVO {

    private Long id;

    private String serverCode;

    private String serverName;

    private String host;

    private Integer sshPort;

    private String osType;

    private String username;

    private Integer connectTimeoutSeconds;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
