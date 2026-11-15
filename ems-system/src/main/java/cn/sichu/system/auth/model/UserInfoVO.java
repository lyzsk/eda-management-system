package cn.sichu.system.auth.model;

import lombok.Data;

import java.util.List;

/**
 *
 * @author sichu huang
 * @since 2026/11/15
 */
@Data
public class UserInfoVO {

    private Long id;

    private String username;

    private String realName;

    private Integer gender;

    private String email;

    private String mobile;

    private String avatar;

    private Long deptId;

    private String deptName;

    private String roleCode;

    private List<String> roles;

    private List<String> permissions;
}
