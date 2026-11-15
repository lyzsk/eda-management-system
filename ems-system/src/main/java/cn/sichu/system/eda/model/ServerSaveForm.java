package cn.sichu.system.eda.model;

import lombok.Data;

/**
 * 服务器新增/编辑表单。凭据来自部署配置，表单不接收密码或私钥。
 *
 * @author sichu huang
 * @since 2026/11/15
 */
@Data
public class ServerSaveForm {

    private String serverCode;

    private String serverName;

    private String host;

    private Integer sshPort;

    private String osType;

    private String username;

    private Integer connectTimeoutSeconds;

    private Integer status;

    private String remark;
}
