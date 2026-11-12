package com.example.blog.service;

import com.example.blog.dto.TagSaveDTO;
import com.example.blog.vo.TagVO;

import java.util.List;

public interface TagService {

    List<TagVO> listAll();

    TagVO create(TagSaveDTO dto);

    void update(Long id, TagSaveDTO dto);

    void delete(Long id);
}
