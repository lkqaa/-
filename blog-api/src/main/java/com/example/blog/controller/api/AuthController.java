package com.example.blog.controller.api;

import com.example.blog.common.Result;
import com.example.blog.dto.LoginDTO;
import com.example.blog.service.AuthService;
import com.example.blog.vo.LoginVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(authService.login(dto));
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
    }

    @GetMapping("/info")
    public Result<LoginVO.AdminInfoVO> info() {
        return Result.success(authService.currentAdmin());
    }
}
