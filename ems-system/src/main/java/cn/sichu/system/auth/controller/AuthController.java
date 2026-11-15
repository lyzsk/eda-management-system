package cn.sichu.system.auth.controller;

import cn.sichu.common.result.Result;
import cn.sichu.system.auth.model.LoginForm;
import cn.sichu.system.auth.model.LoginVO;
import cn.sichu.system.auth.model.UserInfoVO;
import cn.sichu.system.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author sichu huang
 * @since 2026/11/15
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Valid LoginForm form) {
        return Result.success(authService.login(form));
    }

    @GetMapping("/me")
    public Result<UserInfoVO> me() {
        return Result.success(authService.currentUser());
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }
}
