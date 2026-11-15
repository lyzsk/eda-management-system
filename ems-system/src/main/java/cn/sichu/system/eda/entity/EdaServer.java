package cn.sichu.system.eda.entity;

import cn.sichu.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 服务器（SSH 执行目标）。认证凭据由部署配置提供，不入库。
 *
 * @author sichu huang
 * @since 2026/11/15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("eda_server")
public class EdaServer extends BaseEntity {

    private String serverCode;

    private String serverName;

    private String host;

    private Integer sshPort;

    private String osType;

    private String username;

    private String hostKeyFingerprint;

    private Integer connectTimeoutSeconds;

    private Integer status;
}
