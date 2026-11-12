package com.example.blog.service;

import com.example.blog.common.PageResult;
import com.example.blog.dto.CommentCreateDTO;
import com.example.blog.dto.CommentQueryDTO;
import com.example.blog.vo.CommentVO;

import java.util.List;

public interface CommentService {

    PageResult<CommentVO> pagePublic(CommentQueryDTO query);

    CommentVO create(CommentCreateDTO dto, String ip, String userAgent);

    PageResult<CommentVO> pageAdmin(CommentQueryDTO query);

    void updateStatus(Long id, Integer status);

    void delete(Long id);

    void batchDelete(List<Long> ids);
}
