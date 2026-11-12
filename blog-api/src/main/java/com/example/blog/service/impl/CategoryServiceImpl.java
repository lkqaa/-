package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.blog.common.ErrorCode;
import com.example.blog.dto.CategorySaveDTO;
import com.example.blog.entity.Article;
import com.example.blog.entity.Category;
import com.example.blog.exception.BusinessException;
import com.example.blog.mapper.ArticleMapper;
import com.example.blog.mapper.CategoryMapper;
import com.example.blog.service.CategoryService;
import com.example.blog.vo.CategoryVO;
import com.example.blog.vo.CountItemVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final ArticleMapper articleMapper;

    public CategoryServiceImpl(CategoryMapper categoryMapper, ArticleMapper articleMapper) {
        this.categoryMapper = categoryMapper;
        this.articleMapper = articleMapper;
    }

    @Override
    public List<CategoryVO> listAll() {
        Map<Long, Long> counts = articleMapper.countByCategory().stream()
                .collect(Collectors.toMap(CountItemVO::getId, CountItemVO::getCnt));
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                        .orderByAsc(Category::getSortOrder).orderByAsc(Category::getId))
                .stream().map(c -> {
                    CategoryVO vo = new CategoryVO();
                    vo.setId(c.getId());
                    vo.setName(c.getName());
                    vo.setSlug(c.getSlug());
                    vo.setDescription(c.getDescription());
                    vo.setSortOrder(c.getSortOrder());
                    vo.setArticleCount(counts.getOrDefault(c.getId(), 0L));
                    vo.setCreatedAt(c.getCreatedAt());
                    return vo;
                }).toList();
    }

    @Override
    public CategoryVO create(CategorySaveDTO dto) {
        checkSlug(dto.getSlug(), null);
        Category category = new Category();
        fill(category, dto);
        categoryMapper.insert(category);
        CategoryVO vo = new CategoryVO();
        vo.setId(category.getId());
        vo.setName(category.getName());
        vo.setSlug(category.getSlug());
        vo.setDescription(category.getDescription());
        vo.setSortOrder(category.getSortOrder());
        vo.setArticleCount(0L);
        return vo;
    }

    @Override
    public void update(Long id, CategorySaveDTO dto) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        checkSlug(dto.getSlug(), id);
        fill(category, dto);
        category.setId(id);
        categoryMapper.updateById(category);
    }

    @Override
    public void delete(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        Long count = articleMapper.selectCount(new LambdaQueryWrapper<Article>().eq(Article::getCategoryId, id));
        if (count != null && count > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "该分类下仍有 " + count + " 篇文章，无法删除");
        }
        categoryMapper.deleteById(id);
    }

    private void fill(Category category, CategorySaveDTO dto) {
        category.setName(dto.getName());
        category.setSlug(dto.getSlug().trim());
        category.setDescription(dto.getDescription());
        category.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
    }

    private void checkSlug(String slug, Long excludeId) {
        Long count = categoryMapper.selectCount(new LambdaQueryWrapper<Category>()
                .eq(Category::getSlug, slug.trim())
                .ne(excludeId != null, Category::getId, excludeId));
        if (count != null && count > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "slug 已存在：" + slug);
        }
    }
}
