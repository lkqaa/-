package com.example.blog.service.impl;

import com.example.blog.common.ErrorCode;
import com.example.blog.dto.LoginDTO;
import com.example.blog.entity.Admin;
import com.example.blog.exception.BusinessException;
import com.example.blog.mapper.AdminMapper;
import com.example.blog.security.JwtProperties;
import com.example.blog.security.JwtUtils;
import com.example.blog.service.AuthService;
import com.example.blog.vo.LoginVO;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    private final AdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final JwtProperties jwtProperties;

    public AuthServiceImpl(AdminMapper adminMapper, PasswordEncoder passwordEncoder,
                           JwtUtils jwtUtils, JwtProperties jwtProperties) {
        this.adminMapper = adminMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.jwtProperties = jwtProperties;
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        Admin admin = adminMapper.selectByUsername(dto.getUsername());
        if (admin == null || !passwordEncoder.matches(dto.getPassword(), admin.getPassword())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }
        admin.setLastLoginTime(LocalDateTime.now());
        admin.setLastLoginIp("unknown");
        adminMapper.updateById(admin);

        String token = jwtUtils.generate(admin.getId(), admin.getUsername());

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setExpiresIn(jwtProperties.getAccessTokenExpiresIn());

        LoginVO.AdminInfoVO info = new LoginVO.AdminInfoVO();
        info.setId(admin.getId());
        info.setUsername(admin.getUsername());
        info.setNickname(admin.getNickname());
        info.setAvatar(admin.getAvatar());
        info.setEmail(admin.getEmail());
        vo.setAdminInfo(info);
        return vo;
    }

    @Override
    public void logout() {
        SecurityContextHolder.clearContext();
    }

    @Override
    public LoginVO.AdminInfoVO currentAdmin() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof Long adminId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        Admin admin = adminMapper.selectById(adminId);
        if (admin == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        LoginVO.AdminInfoVO info = new LoginVO.AdminInfoVO();
        info.setId(admin.getId());
        info.setUsername(admin.getUsername());
        info.setNickname(admin.getNickname());
        info.setAvatar(admin.getAvatar());
        info.setEmail(admin.getEmail());
        return info;
    }
}
