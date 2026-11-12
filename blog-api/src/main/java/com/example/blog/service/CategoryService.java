package com.example.blog.service;

import com.example.blog.dto.CategorySaveDTO;
import com.example.blog.vo.CategoryVO;

import java.util.List;

public interface CategoryService {

    List<CategoryVO> listAll();

    CategoryVO create(CategorySaveDTO dto);

    void update(Long id, CategorySaveDTO dto);

    void delete(Long id);
}
