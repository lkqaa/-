package com.example.blog.service;

import com.example.blog.dto.LoginDTO;
import com.example.blog.vo.LoginVO;

public interface AuthService {

    LoginVO login(LoginDTO dto);

    void logout();

    LoginVO.AdminInfoVO currentAdmin();
}
