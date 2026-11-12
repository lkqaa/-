package com.example.blog.service;

import com.example.blog.common.PageResult;
import com.example.blog.dto.ArticleQueryDTO;
import com.example.blog.dto.ArticleSaveDTO;
import com.example.blog.vo.ArticleDetailVO;
import com.example.blog.vo.ArticleVO;

import java.util.List;

public interface ArticleService {

    PageResult<ArticleVO> pagePublic(ArticleQueryDTO query);

    ArticleDetailVO detail(Long id);

    Integer incrViews(Long id, String ip, String userAgent);

    PageResult<ArticleVO> pageAdmin(ArticleQueryDTO query);

    ArticleDetailVO adminDetail(Long id);

    Long create(ArticleSaveDTO dto);

    void update(Long id, ArticleSaveDTO dto);

    void delete(Long id);

    void batchDelete(List<Long> ids);

    void updateStatus(Long id, Integer status);
}
