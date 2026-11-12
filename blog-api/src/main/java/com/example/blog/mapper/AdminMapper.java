package com.example.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.entity.Admin;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface AdminMapper extends BaseMapper<Admin> {

    @Select("SELECT * FROM admin WHERE username = #{username}")
    Admin selectByUsername(@Param("username") String username);
}
