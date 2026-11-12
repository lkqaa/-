package com.example.blog.service;

import com.example.blog.entity.SysConfig;

import java.util.List;
import java.util.Map;

public interface SysConfigService {

    Map<String, String> publicConfig();

    List<SysConfig> listAll();

    void updateBatch(Map<String, String> items);
}
