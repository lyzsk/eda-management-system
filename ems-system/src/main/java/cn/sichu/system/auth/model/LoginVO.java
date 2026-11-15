package cn.sichu.system.auth.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 *
 * @author sichu huang
 * @since 2026/11/15
 */
@Data
@AllArgsConstructor
public class LoginVO {

    private String token;

    private UserInfoVO user;
}
